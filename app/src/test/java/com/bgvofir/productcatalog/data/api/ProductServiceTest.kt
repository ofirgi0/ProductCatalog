package com.bgvofir.productcatalog.data.api

import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.ErrorType
import com.bgvofir.productcatalog.repositories.ProductRepository
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

// Runs the real Retrofit + JSON setup against a local server, so wrong paths, query names or
// parsing rules fail here instead of in the app.
class ProductServiceTest {

    private val server = MockWebServer()
    private lateinit var repository: ProductRepository

    private val emptyPage = """{"products":[],"total":0,"skip":0,"limit":20}"""

    @Before
    fun setUp() {
        server.start()
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(ApiJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ProductService::class.java)
        repository = ProductRepository(service)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `list, search and category requests use the API's paths and query names`() = runTest {
        repeat(3) { server.enqueue(MockResponse().setBody(emptyPage)) }

        repository.getProducts(limit = 20, skip = 0)
        repository.searchProducts("phone", limit = 20, skip = 40)
        repository.getProductsByCategory("beauty", limit = 20, skip = 20)

        assertEquals("/products?limit=20&skip=0", server.takeRequest().path)
        assertEquals("/products/search?q=phone&limit=20&skip=40", server.takeRequest().path)
        assertEquals("/products/category/beauty?limit=20&skip=20", server.takeRequest().path)
    }

    @Test
    fun `a 404 from the API maps to NOT_FOUND`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"message":"Product with id '99999' not found"}"""))

        val result = repository.getProduct(99999)

        assertEquals("/products/99999", server.takeRequest().path)
        assertEquals(ErrorType.NOT_FOUND, (result as DataState.Error).type)
    }

    @Test
    fun `categories are parsed from the API's object format`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """[{"slug":"beauty","name":"Beauty","url":"https://dummyjson.com/products/category/beauty"}]""",
            ),
        )

        val result = repository.getCategories()

        assertEquals("/products/categories", server.takeRequest().path)
        assertEquals(DataState.Success(listOf(Category("beauty", "Beauty"))), result)
    }

    @Test
    fun `a malformed body maps to SERVER`() = runTest {
        server.enqueue(MockResponse().setBody("""{"unexpected":true}"""))

        assertEquals(ErrorType.SERVER, (repository.getProduct(1) as DataState.Error).type)
    }
}
