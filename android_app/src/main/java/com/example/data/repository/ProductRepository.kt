package com.example.data.repository

import com.example.data.local.ProductDao
import com.example.data.local.ProductEntity
import com.example.model.ItemCondition
import com.example.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {

    fun observeAll(): Flow<List<Product>> =
        productDao.getAllProducts().map { entities -> entities.map { it.toDomain() } }

    suspend fun getById(id: String): Product? =
        productDao.getProductById(id)?.toDomain()

    suspend fun save(product: Product) {
        productDao.insertProduct(product.toEntity())
    }

    suspend fun saveAll(products: List<Product>) {
        productDao.insertProducts(products.map { it.toEntity() })
    }

    suspend fun delete(id: String) = productDao.deleteProduct(id)

    suspend fun getUnsynced(): List<ProductEntity> = productDao.getUnsyncedProducts()

    suspend fun markSynced(id: String) = productDao.markSynced(id)

    // ──────────────────────────────────
    // Entity ↔ Domain Mappers
    // ──────────────────────────────────

    companion object {
        fun ProductEntity.toDomain(): Product {
            return Product(
                id = id,
                title = title,
                price = price,
                originalPrice = originalPrice,
                businessName = businessName,
                businessId = businessId,
                college = college,
                category = category,
                rating = rating,
                reviewCount = reviewCount,
                description = description,
                inStock = inStock,
                tags = tags.split(",").filter { it.isNotBlank() },
                imageUrl = imageUrl,
                galleryImages = galleryImages.split(",").filter { it.isNotBlank() },
                condition = try { ItemCondition.valueOf(condition) } catch (e: Exception) { ItemCondition.BRAND_NEW },
                semesterTag = semesterTag,
                isRental = isRental,
                rentalDuration = rentalDuration
            )
        }

        fun Product.toEntity(): ProductEntity {
            return ProductEntity(
                id = id,
                title = title,
                price = price,
                originalPrice = originalPrice,
                businessName = businessName,
                businessId = businessId,
                college = college,
                category = category,
                rating = rating,
                reviewCount = reviewCount,
                description = description,
                inStock = inStock,
                tags = tags.joinToString(","),
                imageUrl = imageUrl,
                galleryImages = galleryImages.joinToString(","),
                condition = condition.name,
                semesterTag = semesterTag,
                isRental = isRental,
                rentalDuration = rentalDuration,
                isSyncedWithFirestore = false,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}
