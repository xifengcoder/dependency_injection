package dev.wendyyanto.manual_di_sample.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR)
annotation class Inject