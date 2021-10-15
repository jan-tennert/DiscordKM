object Versions {

    const val KOTLIN = "1.6.0-RC"
    const val DOKKA = "1.5.31"
    const val KTOR = "1.6.4"
    const val SERIALIZATION = "1.3.0"
    const val KORLIBS = "2.2.0"
    const val COROUTINES = "1.5.2"
    const val STATELY = "1.2.0-nmm"
    const val COLORMATH = "3.1.1"

}

object Publishing {

    val REPOSITORY_ID = System.getenv("SONATYPE_REPOSITORY_ID")
    val SONATYPE_USERNAME = System.getenv("SONATYPE_USERNAME")
    val SONATYPE_PASSWORD = System.getenv("SONATYPE_PASSWORD")

}