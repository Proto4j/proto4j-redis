# Proto4j-Redis

A type-safe framework to turn a java interface into a database worker. For more information please visit [the documentation](https://proto4j.github.io/proto4j-redis/).

This repository contains the source code for the `Redis` module from `Proto4j`. It is considered to be a development repository where changes can be made and features can be requested. `SQLFactory` implementation modules are provided in the `factories` directory. Currently, only SQLite and MySQL are provided.

# Example usage

This framework is built to be user-friendly when working with generated service workers. The following example illustrates a simple user service:

````java
@SQL(SQLiteFactory.FACTORY)
public interface UserDao {
    @SQL.Select("select * from {table};")
    public List<User> fetchUsers(@Param("table") String tableName);
}

public static void main(String[]args) {
    SQLiteConfiguration config = new SQLiteConfiguration("mydb");
    UserDao userDao = Redis.create(UserDao.class, config);
    // fetch all user objects
    for (User user : userDao.fetchUsers("user_table1")) {
        // ...
    }
}
````

## Download

Download the [latest JAR file](https://github.com/Proto4j/proto4j-redis/releases) from the releases tab and the `SQLFactory` implementation JAR file what you intend to use. This framework requires a minimum of Java 8+ for developing and running.

## License

    MIT License
    
    Copyright (c) 2022 Proto4j
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
