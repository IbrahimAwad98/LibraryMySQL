# LibraryMySQL

A JavaFX desktop client for a MySQL library database — search books, view
authors and genres, and rate what you have read.

Built with Ahmed El Yasini as a pair project for HI1030 Databasteknik at KTH.

## What works

- **Search** by title, ISBN, author, genre, or rating. Title, author and genre
  searches are partial matches; ISBN is exact. Rating matches books whose
  *rounded average* equals the number you enter — it is an equality test, not a
  minimum.
- **Book details** on row selection: authors with birth dates, genres, and the
  reviews left by other users.
- **Rate a book** from 1 to 10, one rating per user per book, enforced by a
  database check before the insert.
- **Add and delete books**, with authors and genres attached at creation.
- **Reviews** — write a text review, read everyone else's.
- **Accounts** — register (username at least 3 characters, password at least 4)
  and log in. Book management stays disabled until you are logged in.

Under the hood: MVC with the controller depending only on `BooksDbInterface`, a
singleton `DatabaseConnection`, and prepared statements for every query that
takes user input. Long queries run on JavaFX `Task` threads so the window does
not freeze.

## Requirements

- **JDK 21** — the POM targets 21, so JDK 17 will not build this.
- **MySQL 8.0.16 or newer** — the schema uses `CHECK` constraints, which older
  servers parse and ignore.
- Maven is not required; the repository ships the Maven wrapper.

## Database setup

The schema, the two database accounts, and the seed data all live in one file.

> **This script is destructive.** Its first statement is
> `DROP DATABASE IF EXISTS LibraryDB;`. If you already have a database named
> `LibraryDB`, it will be removed without warning.

It creates users and grants privileges, so it must be run as **root** or an
account with `CREATE USER` and `GRANT OPTION`:

```bash
mysql -u root -p < Database/LibraryDB.sql
```

Do not pass a database name — the script creates and selects `LibraryDB`
itself. It sets up eight tables (`T_User`, `T_Author`, `T_Genre`, `T_Book`,
`T_Rating`, `T_Review`, `T_Book_Author`, `T_Book_Genre`) and seeds them with
four users, five authors, seven genres and five books.

The application connects to `localhost:3306` as the `library_client` account
the script creates. Host, user and password are compile-time constants in
`src/main/java/se/kth/awad/bookdatabase/model/DatabaseConnection.java`; changing
them means editing that file and rebuilding.

## Building and running

```bash
./mvnw clean package      # Linux / macOS
mvnw.cmd clean package    # Windows
```

**The run goal is currently misconfigured and will fail.** `module-info.java`
declares the module as `BookDatabase`, but the javafx-maven-plugin's
`<mainClass>` in `pom.xml` is
`se.kth.awad.bookdatabase/se.kth.awad.bookdatabase.App` — the part before the
slash must be the module name, not the package name. Running `./mvnw javafx:run`
as configured produces:

```
java.lang.module.FindException: Module se.kth.awad.bookdatabase not found
```

Correcting `<mainClass>` to `BookDatabase/se.kth.awad.bookdatabase.App` fixes
the mismatch. There is no other launch path in the repository — no shaded jar,
no exec plugin, no launcher script.

Once the application starts, connect with **Database → Connect** before using
any other menu.

## Known gaps

- **`javafx:run` does not work as configured** — see above. This is the one
  thing standing between a clone and a running application.
- **Passwords are stored and compared in plaintext.** `T_User.password` holds
  the raw string, the seed data ships readable passwords, and `login()` compares
  them in SQL. There is no hashing anywhere.
- **Database credentials are committed to the repository**, in both
  `DatabaseConnection.java` and `Database/LibraryDB.sql`. The script also creates
  an `Admin` account with `ALL PRIVILEGES ON *.*` that the application never
  uses.

## License

[MIT](LICENSE)
