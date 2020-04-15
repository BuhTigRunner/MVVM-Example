package com.borisenkoda.mobile.mvvmdatabinding.base.failure


sealed class Failure {
    object NetworkConnection : Failure()
    object ServerError : Failure()
    object AuthError : Failure()
    object FileReadingError: Failure()
    object DbError: Failure()

    /** * Extend this class for feature specific failures.*/
    abstract class FeatureFailure : Failure()
}
