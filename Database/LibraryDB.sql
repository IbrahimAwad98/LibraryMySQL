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
CREATE TABLE T_Author(
    authorID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    birthDate DATE
);

CREATE TABLE T_Genre(
    genreID INT AUTO_INCREMENT PRIMARY KEY,
    genreName VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE T_Book(
    bookID INT AUTO_INCREMENT PRIMARY KEY,
    ISBN VARCHAR(13) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    publishedDate DATE
);

CREATE TABLE T_Rating(
    ratingID INT AUTO_INCREMENT PRIMARY KEY,
    bookID INT NOT NULL,
    ratingValue INT NOT NULL CHECK (ratingValue BETWEEN 1 AND 10),
    ratingDate DATE NOT NULL,
    FOREIGN KEY (bookID) REFERENCES T_Book(bookID) ON DELETE CASCADE
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

-- fyll i data
INSERT INTO T_Author (name, birthDate) VALUES
('Miguel de Cervantes', '1547-09-29'),
('Charles Dickens', '1812-02-07'),
('J.R.R. Tolkien', '1892-01-03'),
('Antoine de Saint-Exupéry', '1900-06-29'),
('J.K. Rowling', '1965-07-31');

INSERT INTO T_Genre (genreName) VALUES
('Fantasy'),          
('Classic'),
('Adventure'),
('Historical Fiction'),
('Philosophical'),
('Young Adult'),
('Drama');


INSERT INTO T_Book (ISBN, title, publishedDate) VALUES
('9780060934347', 'Don Quixote', '1605-01-16'),
('9780486406510', 'A Tale of Two Cities', '1859-04-30'),
('9780618640157', 'The Lord of the Rings', '1954-07-29'),
('9780156012195', 'The Little Prince', '1943-04-06'),
('9780439708180', 'Harry Potter and the Philosopher''s Stone', '1997-06-26');

INSERT INTO T_Book_Author (bookID, authorID) VALUES
(1, 1),  
(2, 2),  
(3, 3),  
(4, 4),  
(5, 5);  

INSERT INTO T_Book_Genre (bookID, genreID) VALUES
(1, 1), (1, 2),          
(2, 1), (2, 4), (2, 7),  
(3, 3), (3, 2), (3, 1),  
(4, 5), (4, 1),          
(5, 3), (5, 6), (5, 2); 


INSERT INTO T_Rating (bookID, ratingValue, ratingDate) VALUES
(1, 7, '2024-01-15'), (1, 8, '2024-01-20'), (1, 9, '2024-01-25'),
(2, 6, '2024-02-01'), (2, 7, '2024-02-05'), (2, 8, '2024-02-10'),
(3, 9, '2024-03-01'), (3, 10, '2024-03-05'), (3, 10, '2024-03-10'),
(4, 8, '2024-04-01'), (4, 9, '2024-04-05'), (4, 9, '2024-04-10'),
(5, 5, '2024-05-01'), (5, 6, '2024-05-05'), (5, 7, '2024-05-10');

-- se allt ihop
/*
SELECT 
    b.title,
    b.ISBN,
    GROUP_CONCAT(DISTINCT a.name SEPARATOR ', ') as authors,
    GROUP_CONCAT(DISTINCT g.genreName SEPARATOR ', ') as genres,
    AVG(r.ratingValue) as avgRating,
    MAX(r.ratingDate) as latestRatingDate
FROM T_Book b
LEFT JOIN T_Book_Author ba ON b.bookID = ba.bookID
LEFT JOIN T_Author a ON ba.authorID = a.authorID
LEFT JOIN T_Book_Genre bg ON b.bookID = bg.bookID
LEFT JOIN T_Genre g ON bg.genreID = g.genreID
LEFT JOIN T_Rating r ON b.bookID = r.bookID
GROUP BY b.bookID, b.title, b.ISBN;
*/

-- skapa användare med begränsat behörighet. 
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