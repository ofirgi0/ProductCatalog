package com.bgvofir.productcatalog.repositories

import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.ErrorType
import com.bgvofir.productcatalog.data.api.Category
import com.bgvofir.productcatalog.data.api.Product
import com.bgvofir.productcatalog.data.api.ProductService
import com.bgvofir.productcatalog.data.api.ProductsResponse
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

// Retrofit's suspend functions are main-safe, so no dispatcher switching is needed here.
class ProductRepository @Inject constructor(
    private val service: ProductService,
) : IProductRepository {

    override suspend fun getProducts(limit: Int, skip: Int): DataState<ProductsResponse> =
        safeCall { service.getProducts(limit, skip) }

    override suspend fun searchProducts(query: String, limit: Int, skip: Int): DataState<ProductsResponse> =
        safeCall { service.searchProducts(query, limit, skip) }

    override suspend fun getProductsByCategory(slug: String, limit: Int, skip: Int): DataState<ProductsResponse> =
        safeCall { service.getProductsByCategory(slug, limit, skip) }

    override suspend fun getProduct(id: Int): DataState<Product> =
        safeCall { service.getProduct(id) }

    override suspend fun getCategories(): DataState<List<Category>> =
        safeCall { service.getCategories() }

    private suspend fun <T> safeCall(call: suspend () -> T): DataState<T> =
        try {
            DataState.Success(call())
        } catch (e: CancellationException) {
            // Never swallow cancellation, or a cancelled screen would keep running.
            throw e
        } catch (e: IOException) {
            DataState.Error(ErrorType.NETWORK, e)
        } catch (e: HttpException) {
            DataState.Error(if (e.code() == 404) ErrorType.NOT_FOUND else ErrorType.SERVER, e)
        } catch (e: SerializationException) {
            DataState.Error(ErrorType.SERVER, e)
        }
}
