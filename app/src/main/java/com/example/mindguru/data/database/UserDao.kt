import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?
}