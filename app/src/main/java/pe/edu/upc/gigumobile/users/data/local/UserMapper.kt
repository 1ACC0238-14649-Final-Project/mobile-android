package pe.edu.upc.gigumobile.users.data.local

import pe.edu.upc.gigumobile.users.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        email = this.email,
        name = this.name,
        lastname = this.lastname,
        role = this.role,
        image = this.image,
        token = this.token
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        email = this.email,
        name = this.name,
        lastname = this.lastname,
        role = this.role,
        image = this.image,
        token = this.token
    )
}
