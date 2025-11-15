package edu.ucne.faemaciacruz.domain.usecase.producto

import edu.ucne.faemaciacruz.data.repository.ProductRepositoryImpl
import edu.ucne.faemaciacruz.domain.model.Producto
import edu.ucne.faemaciacruz.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchProductosUseCase @Inject constructor(
    private val productRepositoryImpl: ProductRepositoryImpl
) {
    suspend operator fun invoke(query: String): Flow<Resource<List<Producto>>> {
        return productRepositoryImpl.searchProductos(query)
    }
}