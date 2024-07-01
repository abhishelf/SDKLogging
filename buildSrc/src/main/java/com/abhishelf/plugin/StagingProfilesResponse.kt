package com.abhishelf.plugin

internal data class Repository(
    val repositoryId: String,
    val transitioning: Boolean,
    val type: String
)

internal data class ProfileRepositoriesResponse(val data: List<Repository>)

internal data class TransitionRepositoryInputData(
    val stagedRepositoryId: String,
    val targetRepositoryId: String
)

internal data class TransitionRepositoryInput(val data: TransitionRepositoryInputData)