package edu.ucne.farmaciacruz.domain.usecase.producto

import edu.ucne.farmaciacruz.data.repository.ProductRepositoryImpl
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchProductosUseCase @Inject constructor(
    private val productRepositoryImpl: ProductRepositoryImpl
) {
    suspend operator fun invoke(query: String): Flow<Resource<List<Producto>>> {
        return productRepositoryImpl.searchProductos(query)
    }
}