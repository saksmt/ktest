class ClassWhichWouldBeUsedForMapping
val youCanAlsoUsePojoAsSourceOfParameters = ClassWhichWouldBeUsedForMapping()
class Entity1
class Entity2

class Dao {
    fun saveEntity1(any: Any) {}
    fun saveEntity2(any: Any) {}

    fun removeEntity(any: Any) {}
}

val dao = Dao()
