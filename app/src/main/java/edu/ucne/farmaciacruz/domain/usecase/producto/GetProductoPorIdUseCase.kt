package edu.ucne.farmaciacruz.domain.usecase.producto

import edu.ucne.farmaciacruz.data.repository.ProductRepositoryImpl
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductoPorIdUseCase @Inject constructor(
    private val productRepositoryImpl: ProductRepositoryImpl
) {
    suspend operator fun invoke(id: Int): Flow<Resource<Producto>> {
        return productRepositoryImpl.getProducto(id)
    }
}