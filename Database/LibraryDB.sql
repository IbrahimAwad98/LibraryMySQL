-- ta bort befintliga användare och börja om
DROP DATABASE IF EXISTS LibraryDB;
DROP USER IF EXISTS 'Admin'@'localhost';
DROP USER IF EXISTS 'library_client'@'localhost';

-- skapa admin (hela behörighet)
CREATE USER 'Admin'@'localhost' IDENTIFIED BY 'Admin2025';
GRANT ALL PRIVILEGES ON *.* TO 'Admin'@'localhost';

-- skapa databas
CREATE DATABASE LibraryDB;
USE LibraryDB;

-- Tabeller
CREATE TABLE T_User(
    userID INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE T_Author(
    authorID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    birthDate DATE,
    addedByUserID INT NOT NULL,
    FOREIGN KEY (addedByUserID) REFERENCES T_User(userID)
);

CREATE TABLE T_Genre(
    genreID INT AUTO_INCREMENT PRIMARY KEY,
    genreName VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE T_Book(
    bookID INT AUTO_INCREMENT PRIMARY KEY,
    ISBN VARCHAR(13) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    publishedDate DATE,
    addedByUserID INT NOT NULL,
    FOREIGN KEY (addedByUserID) REFERENCES T_User(userID)
);

CREATE TABLE T_Rating(
    ratingID INT AUTO_INCREMENT PRIMARY KEY,
    bookID INT NOT NULL,
    userID INT NOT NULL,
    ratingValue INT NOT NULL CHECK (ratingValue BETWEEN 1 AND 10),
    ratingDate DATE NOT NULL,
    FOREIGN KEY (bookID) REFERENCES T_Book(bookID) ON DELETE CASCADE,
    FOREIGN KEY (userID) REFERENCES T_User(userID),  
    UNIQUE KEY unique_user_book_rating (bookID, userID)  -- EN RATING PER USER PER BOK
);

CREATE TABLE T_Review(
    reviewID INT PRIMARY KEY AUTO_INCREMENT,
    bookID INT NOT NULL,
    userID INT NOT NULL,
    reviewText TEXT NOT NULL,
    reviewDate DATE NOT NULL,
    FOREIGN KEY (bookID) REFERENCES T_Book(bookID) ON DELETE CASCADE,
    FOREIGN KEY (userID) REFERENCES T_User(userID) ON DELETE CASCADE  
);

CREATE TABLE T_Book_Author(
    bookID INT NOT NULL,
    authorID INT NOT NULL,
    PRIMARY KEY (bookID, authorID),
    FOREIGN KEY (bookID) REFERENCES T_Book(bookID) ON DELETE CASCADE,
    FOREIGN KEY (authorID) REFERENCES T_Author(authorID) ON DELETE CASCADE
);

CREATE TABLE T_Book_Genre(
    bookID INT NOT NULL,
    genreID INT NOT NULL,
    PRIMARY KEY (bookID, genreID),
    FOREIGN KEY (bookID) REFERENCES T_Book(bookID) ON DELETE CASCADE,
    FOREIGN KEY (genreID) REFERENCES T_Genre(genreID) ON DELETE CASCADE
);

-- Lägg till testanvändare
INSERT INTO T_User (username, password) VALUES
('testuser', 'password123'),
('admin', 'admin123'),
('ibrahim', 'ibra123'),
('anna', 'anna321');

-- fyll i data
INSERT INTO T_Author (name, birthDate, addedByUserID) VALUES
('Miguel de Cervantes', '1547-09-29', 1),
('Charles Dickens', '1812-02-07', 2),
('J.R.R. Tolkien', '1892-01-03', 3),
('Antoine de Saint-Exupéry', '1900-06-29', 4),
('J.K. Rowling', '1965-07-31', 1);

INSERT INTO T_Genre (genreName) VALUES
('Fantasy'),          
('Classic'),
('Adventure'),
('Historical Fiction'),
('Philosophical'),
('Young Adult'),
('Drama');

INSERT INTO T_Book (ISBN, title, publishedDate, addedByUserID) VALUES
('9780060934347', 'Don Quixote', '1605-01-16',2 ),
('9780486406510', 'A Tale of Two Cities', '1859-04-30', 2),
('9780618640157', 'The Lord of the Rings', '1954-07-29', 2),
('9780156012195', 'The Little Prince', '1943-04-06', 2),
('9780439708180', 'Harry Potter and the Philosopher''s Stone', '1997-06-26', 2);

INSERT INTO T_Book_Author (bookID, authorID) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5);

INSERT INTO T_Book_Genre (bookID, genreID) VALUES
(1, 1), (1, 2),          
(2, 1), (2, 4), (2, 7),  
(3, 3), (3, 2), (3, 1),  
(4, 5), (4, 1),          
(5, 3), (5, 6), (5, 2); 


INSERT INTO T_Rating (bookID, userID, ratingValue, ratingDate) VALUES
(1, 1, 7, '2024-01-15'),   -- user 1 ger betyg 7
(1, 2, 8, '2024-01-20'),   -- user 2 ger betyg 8
(1, 3, 9, '2024-01-25'),   -- user 3 ger betyg 9

(2, 1, 6, '2024-02-01'),   -- user 1 ger betyg 6
(2, 3, 7, '2024-02-05'),   -- user 3 ger betyg 7
(2, 4, 8, '2024-02-10'),   -- user 4 ger betyg 8

(3, 2, 9, '2024-03-01'),   -- user 2 ger betyg 9
(3, 3, 10, '2024-03-05'),  -- user 3 ger betyg 10
(3, 4, 10, '2024-03-10'),  -- user 4 ger betyg 10

(4, 1, 8, '2024-04-01'),   -- user 1 ger betyg 8
(4, 2, 9, '2024-04-05'),   -- user 2 ger betyg 9
(4, 4, 9, '2024-04-10'),   -- user 4 ger betyg 9

(5, 1, 5, '2024-05-01'),   -- user 1 ger betyg 5
(5, 3, 6, '2024-05-05'),   -- user 3 ger betyg 6
(5, 4, 7, '2024-05-10');   -- user 4 ger betyg 7

INSERT INTO T_Review (bookID, userID, reviewText, reviewDate) VALUES
(1, 1, 'En fantastisk klassiker! Mycket rolig.', '2024-01-15'),
(3, 2, 'Bästa fantasy-serien någonsin.', '2024-03-01');

-- skapa användare med begränsat behörighet
CREATE USER 'library_client'@'localhost' 
IDENTIFIED BY 'LibClient2025!';

GRANT SELECT, INSERT, UPDATE, DELETE ON LibraryDB.T_Book 
TO 'library_client'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON LibraryDB.T_Author 
TO 'library_client'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON LibraryDB.T_Genre 
TO 'library_client'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON LibraryDB.T_Book_Author 
TO 'library_client'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON LibraryDB.T_Book_Genre 
TO 'library_client'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON LibraryDB.T_Rating 
TO 'library_client'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON LibraryDB.T_User 
TO 'library_client'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON LibraryDB.T_Review 
TO 'library_client'@'localhost';