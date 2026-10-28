package com.bgvofir.productcatalog.repositories

import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.ErrorType
import com.bgvofir.productcatalog.data.api.Category
import com.bgvofir.productcatalog.data.api.Product
import com.bgvofir.productcatalog.data.api.ProductService
import com.bgvofir.productcatalog.data.api.ProductsResponse
import com.bgvofir.productcatalog.data.api.sampleProduct
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ProductRepositoryTest {

    private val service = FakeProductService()
    private val repository = ProductRepository(service)

    @Test
    fun `success is wrapped in Success`() = runTest {
        service.product = sampleProduct(id = 7)

        assertEquals(DataState.Success(sampleProduct(id = 7)), repository.getProduct(7))
    }

    @Test
    fun `IOException maps to NETWORK`() = runTest {
        service.error = IOException("offline")

        assertEquals(ErrorType.NETWORK, (repository.getProducts(20, 0) as DataState.Error).type)
    }

    @Test
    fun `HTTP 404 maps to NOT_FOUND`() = runTest {
        service.error = httpError(404)

        assertEquals(ErrorType.NOT_FOUND, (repository.getProduct(99999) as DataState.Error).type)
    }

    @Test
    fun `other HTTP errors map to SERVER`() = runTest {
        service.error = httpError(500)

        assertEquals(ErrorType.SERVER, (repository.getCategories() as DataState.Error).type)
    }

    @Test
    fun `malformed JSON maps to SERVER`() = runTest {
        service.error = SerializationException("bad json")

        assertEquals(ErrorType.SERVER, (repository.searchProducts("phone", 20, 0) as DataState.Error).type)
    }

    @Test
    fun `cancellation is rethrown, not turned into an error`() = runTest {
        service.error = CancellationException("screen closed")

        try {
            repository.getProductsByCategory("beauty", 20, 0)
            fail("expected CancellationException")
        } catch (_: CancellationException) {
        }
    }

    @Test
    fun `query, paging and slug are passed through to the service`() = runTest {
        repository.searchProducts("phone", 20, 40)
        assertEquals("search:phone:20:40", service.lastCall)

        repository.getProductsByCategory("beauty", 20, 0)
        assertEquals("category:beauty:20:0", service.lastCall)
    }

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))
}

private class FakeProductService : ProductService {
    var error: Throwable? = null
    var product: Product = sampleProduct()
    var lastCall: String? = null

    private val page get() = ProductsResponse(listOf(product), total = 1, skip = 0, limit = 20)

    private fun <T> respond(call: String, value: T): T {
        lastCall = call
        error?.let { throw it }
        return value
    }

    override suspend fun getProducts(limit: Int, skip: Int) = respond("all:$limit:$skip", page)

    override suspend fun searchProducts(query: String, limit: Int, skip: Int) =
        respond("search:$query:$limit:$skip", page)

    override suspend fun getProductsByCategory(slug: String, limit: Int, skip: Int) =
        respond("category:$slug:$limit:$skip", page)

    override suspend fun getProduct(id: Int) = respond("product:$id", product)

    override suspend fun getCategories() = respond("categories", listOf(Category("beauty", "Beauty")))
}
