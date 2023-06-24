package dev.wendyyanto.dependency_lib.di

import dev.wendyyanto.dependency_lib.annotation.Inject
import dev.wendyyanto.dependency_lib.annotation.Provides
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass

object Injectors {

    //provide method -> 该method的return type的Class实例
    private val methodToClassMap: MutableMap<Method, Class<*>> by lazy {
        mutableMapOf()
    }

    //provide method的return type的Class实例 -> 该method
    private val classToMethodMap: MutableMap<Class<*>, Method> by lazy {
        mutableMapOf()
    }

    //provide method -> 该method所依赖的类的列表
    private val methodDependencies: MutableMap<Method, List<Class<*>>> by lazy {
        mutableMapOf()
    }

    //provide method -> 该method所依赖的provide method的集合
    private val methodTree: MutableMap<Method, MutableSet<Method>> by lazy {
        mutableMapOf()
    }

    //Class<*> -> 该类的实例对象
    private val appDependencies: MutableMap<Class<*>, Any> by lazy {
        mutableMapOf()
    }

    fun <T : InjectorModule> injectApp(kClass: KClass<T>) {
        saveMethods(kClass)
        generateMethodDependencyTree()

        methodTree.keys.forEach { method ->
            constructDependenciesByDFS(method, kClass.java.newInstance(), appDependencies)
        }

        cleanUp()
    }

    /**
     * @param moduleClass Class<T:InjectorModule>的实例
     * @param entryPointClass 需要使用依赖注入的类的实例
     */
    fun <T : InjectorModule, R : Any> inject(moduleClass: KClass<T>, entryPointClass: R) {
        val dependencies = appDependencies.toMutableMap()
        saveMethods(moduleClass)
        generateMethodDependencyTree()
        val moduleInstance: T = moduleClass.java.newInstance()

        // Construct and inject dependencies
        entryPointClass.javaClass.fields
            .filter { field ->
                field.isAnnotationPresent(Inject::class.java)
            }
            .onEach { field ->
                constructAndCacheDependencies(field, moduleInstance, dependencies)
            }.forEach { field ->
                field.set(entryPointClass, dependencies[field.type])
            }

        cleanUp()
    }

    private fun <T : InjectorModule> saveMethods(kClass: KClass<T>) {
        kClass.java.declaredMethods
            .filter { method -> method.isAnnotationPresent(Provides::class.java) }
            .forEach {
                saveMethod(it)
            }
    }

    private fun <T : InjectorModule> constructAndCacheDependencies(
        field: Field,
        moduleInstance: T,
        dependencies: MutableMap<Class<*>, Any>
    ) {
        if (dependencies.containsKey(field.type)) {
            return
        }

        val rootMethod = classToMethodMap[field.type]
        val safeRootMethod =
            rootMethod ?: throw IllegalArgumentException("Should have root entry point")
        constructDependenciesByDFS(safeRootMethod, moduleInstance, dependencies)
    }

    private fun saveMethod(method: Method) {
        methodToClassMap[method] = method.returnType
        classToMethodMap[method.returnType] = method
        methodDependencies[method] = method.parameterTypes.toList()
    }

    private fun <T> constructDependenciesByDFS(
        method: Method,
        moduleInstance: T,
        dependencies: MutableMap<Class<*>, Any>
    ) {
        println(">>>>>" + method.name + ", module: " + moduleInstance!!::class.java.simpleName + ", dependencies: " + dependencies)
        val methodDependencies = methodTree[method].orEmpty()
        methodDependencies.forEach { methodDependency ->
            constructDependenciesByDFS(methodDependency, moduleInstance, dependencies)
        }

        val parameters = this.methodDependencies[method]
            ?.map { clazz -> dependencies[clazz] }
            ?.toTypedArray()
            .orEmpty()

        println(">>>>>   method: " + method.name + ", parameters: " + parameters.size)

        methodToClassMap[method]?.let { safeClass ->
            dependencies[safeClass] = method.invoke(moduleInstance, *parameters)
        }
    }

    private fun generateMethodDependencyTree() {
        methodToClassMap.keys.forEach { method ->
            lookupMethodDependencyTree(method)
        }
    }

    private fun lookupMethodDependencyTree(method: Method) {
        if (methodTree[method] == null) {
            methodTree[method] = mutableSetOf()
        }
        val safeMethods: MutableList<Method> = mutableListOf()
        methodDependencies[method]?.forEach { clazz ->
            classToMethodMap[clazz]?.let { safeMethod ->
                safeMethods.add(safeMethod)
            }
        }
        methodTree[method]?.addAll(safeMethods)
    }

    private fun cleanUp() {
        methodToClassMap.clear()
        classToMethodMap.clear()
        methodDependencies.clear()
        methodTree.clear()
    }
}