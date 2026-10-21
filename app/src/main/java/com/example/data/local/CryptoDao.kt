package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoDao {
    // Carteiras (Wallets)
    @Query("SELECT * FROM carteiras ORDER BY nome ASC")
    fun getAllCarteiras(): Flow<List<CarteiraEntity>>

    @Query("SELECT * FROM carteiras ORDER BY nome ASC")
    suspend fun getAllCarteirasList(): List<CarteiraEntity>

    @Query("SELECT * FROM carteiras WHERE id = :id")
    suspend fun getCarteiraById(id: Long): CarteiraEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarteira(carteira: CarteiraEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarteiras(carteiras: List<CarteiraEntity>)

    @Update
    suspend fun updateCarteira(carteira: CarteiraEntity)

    @Query("DELETE FROM carteiras WHERE id = :id")
    suspend fun deleteCarteiraById(id: Long)

    @Query("DELETE FROM carteiras")
    suspend fun clearCarteiras()

    // Lançamentos (Operations/Transactions)
    @Query("SELECT * FROM lancamentos ORDER BY timestamp ASC")
    fun getAllLancamentos(): Flow<List<LancamentoEntity>>

    @Query("SELECT * FROM lancamentos ORDER BY timestamp ASC")
    suspend fun getAllLancamentosList(): List<LancamentoEntity>

    @Query("SELECT * FROM lancamentos WHERE carteiraId = :carteiraId ORDER BY timestamp ASC")
    fun getLancamentosByCarteira(carteiraId: Long): Flow<List<LancamentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLancamento(lancamento: LancamentoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLancamentos(lancamentos: List<LancamentoEntity>)

    @Update
    suspend fun updateLancamento(lancamento: LancamentoEntity)

    @Query("DELETE FROM lancamentos WHERE id = :id")
    suspend fun deleteLancamentoById(id: Long)

    @Query("DELETE FROM lancamentos")
    suspend fun clearLancamentos()

    // Tax Transactions (Extratos de Compras e Vendas - legacy/bridge compatibility)
    @Query("SELECT * FROM tax_transactions ORDER BY timestamp ASC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM tax_transactions ORDER BY timestamp ASC")
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM tax_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM tax_transactions")
    suspend fun clearTransactions()

    // Portfolio
    @Query("SELECT * FROM portfolio_items ORDER BY timestamp DESC")
    fun getAllPortfolioItems(): Flow<List<PortfolioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolioItem(item: PortfolioEntity): Long

    @Delete
    suspend fun deletePortfolioItem(item: PortfolioEntity)

    @Query("DELETE FROM portfolio_items WHERE id = :id")
    suspend fun deletePortfolioItemById(id: Long)

    @Query("SELECT * FROM portfolio_items ORDER BY timestamp DESC")
    suspend fun getAllPortfolioItemsList(): List<PortfolioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolioItems(items: List<PortfolioEntity>)

    @Query("DELETE FROM portfolio_items")
    suspend fun clearPortfolio()

    // Favorites
    @Query("SELECT * FROM favorite_coins")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorite_coins")
    suspend fun getAllFavoritesList(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM favorite_coins WHERE coinId = :coinId")
    suspend fun removeFavorite(coinId: String)

    @Query("DELETE FROM favorite_coins")
    suspend fun clearFavorites()

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_coins WHERE coinId = :coinId)")
    fun isFavorite(coinId: String): Flow<Boolean>
}
