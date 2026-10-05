package com.example.data.repository

import com.example.data.local.FeedPostDao
import com.example.data.local.FeedPostEntity
import com.example.model.CampusFeedPost
import com.example.model.FeedCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val feedPostDao: FeedPostDao
) {

    fun observeAll(): Flow<List<CampusFeedPost>> =
        feedPostDao.getAllPosts().map { list -> list.map { it.toDomain() } }

    suspend fun getAllOnce(): List<CampusFeedPost> =
        feedPostDao.getAllPostsOnce().map { it.toDomain() }

    suspend fun getById(id: String): CampusFeedPost? =
        feedPostDao.getPostById(id)?.toDomain()

    suspend fun save(post: CampusFeedPost) {
        feedPostDao.insertPost(post.toEntity())
    }

    suspend fun saveAll(posts: List<CampusFeedPost>) {
        feedPostDao.insertPosts(posts.map { it.toEntity() })
    }

    suspend fun delete(id: String) = feedPostDao.deletePost(id)

    suspend fun clearAll() = feedPostDao.clearAllPosts()

    suspend fun getUnsynced(): List<FeedPostEntity> = feedPostDao.getUnsyncedPosts()

    suspend fun markSynced(id: String) = feedPostDao.markSynced(id)

    companion object {
        fun FeedPostEntity.toDomain(): CampusFeedPost {
            val cat = try { FeedCategory.valueOf(category) } catch (e: Exception) { FeedCategory.ALL }
            return CampusFeedPost(
                id = id,
                authorName = authorName,
                authorRole = authorRole,
                college = college,
                timestamp = timestamp,
                category = cat,
                content = content,
                tag = tag,
                likes = likes,
                commentsCount = commentsCount
            )
        }

        fun CampusFeedPost.toEntity(): FeedPostEntity {
            return FeedPostEntity(
                id = id,
                authorName = authorName,
                authorRole = authorRole,
                college = college,
                timestamp = timestamp,
                category = category.name,
                content = content,
                tag = tag,
                likes = likes,
                commentsCount = commentsCount,
                isSyncedWithFirestore = false,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}
