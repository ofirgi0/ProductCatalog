package com.bgvofir.productcatalog.di

import com.bgvofir.productcatalog.repositories.CartRepository
import com.bgvofir.productcatalog.repositories.ICartRepository
import com.bgvofir.productcatalog.repositories.IProductRepository
import com.bgvofir.productcatalog.repositories.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepository): IProductRepository

    // Singleton so every screen shares the same cart.
    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepository): ICartRepository
}
