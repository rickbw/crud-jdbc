Crud JDBC
=========

This project provides an implementation of the [Crud API](https://github.com/rickbw/crud-api) for JDBC.

The implementation is quite basic at this time. Two `Resource` types are currently supported:
* `SELECT`: [JdbcReadableResource](https://github.com/rickbw/crud-jdbc/blob/master/src/main/java/rickbw/crud/jdbc/JdbcReadableResource.java), a [ReadableResource](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/ReadableResource.java)
* `INSERT`, `UPDATE`, `DELETE`: [JdbcUpdatableResource](https://github.com/rickbw/crud-jdbc/blob/master/src/main/java/rickbw/crud/jdbc/JdbcUpdatableResource.java), an [UpdatableResource](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/UpdatableResource.java)

SQL statements are passed into the API via JDBC-compatible `PreparedStatement` strings (with optional placeholders for parameters). Therefore, the API is not able to differentiate among insertions, updates, or deletions, so it models them all uniformly with a single `UpdatableResource` implementation.

Most applications will not use the above `Resource` implementation classes directly. Instead, they will start with the corresponding `ResourceProviders`, which implement URI-based lookup of particular `Resources`. For example, [JdbcReadableResourceProvider](https://github.com/rickbw/crud-jdbc/blob/master/src/main/java/rickbw/crud/jdbc/JdbcReadableResourceProvider.java) provides instances of `JdbcReadableResource` on demand.


See Also
--------
* The [Crud API](https://github.com/rickbw/crud-api) project (`crud-api`) defines the core abstractions and the public API on which this project is based.
* `crud-api` is built on top of [RxJava](https://github.com/Netflix/RxJava/).
* [Crud HTTP](https://github.com/rickbw/crud-http) (`crud-http`) is a sister project to this project, implemented for HTTP instead of JDBC.
* [Crud Voldemort](https://github.com/rickbw/crud-voldemort) (`crud-voldemort`) is a sister project to this project, implemented for [Project Voldemort](http://www.project-voldemort.com) instead of JDBC.


Copyright and License
---------------------
All files in this project are copyright Rick Warren and, unless otherwise noted, licensed under the terms of the Apache 2 license.
