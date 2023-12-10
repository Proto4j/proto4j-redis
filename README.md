This is the official documentation to the `Proto4j` module `Redis`. This framework is designed to implement a defined Java interface.

The developer documentation is provided [here](https://proto4j.github.io/proto4j-redis/javadoc/index.html). Alternatively, the source code is heavily documented, which should make it easier to understand the system.

## Example

Suppose we have a database service named `UserStorage` which is intend to manage a user database. In this case the class will delcare two abstract methods:
````java
public abstract List<User> fetchUsersFrom(String table);
public abstract void addUser(int id, String name);
````
Each method has to be annotated with a corresponding SQL-action. Here for example,
````java
@SQL.Select("select * from {table}")
public abstract List<User> fetchUsersFrom(@Param("table") String table);

@SQL.Insert("insert into {table} values ({id}, {name});
public abstract void addUser(@Param("id") int id, @Param("name") String name);
````

Before creating the `UserStorage` implemantation, the used `SQLFactory` has to be specified. In this case we use the `SQLiteFactory` which is also included in this repository. 

````java
@SQL(SQLiteFactory.FACTORY)
public interface UserStorage {
//[...]
}
````
Now, we are ready to create the instance by calling `Redis.create(...)` with a provided configuration or source:

````java
SQLConfiguration config = new SQLiteConfiguration("mydb");
UserStorage storage = Redis.create(UserStorage.class, config);
````

## Individual `SQLFactory` implementations

Take the sqlite factory structure and implementation as the base for newer implementations of the SQLFactory. It is important to create the `META-INF/services/` directory with a file named with the full SQLFactory name (+path). Only if this step is done, the service will be loaded by the `FactoryManager`. 
