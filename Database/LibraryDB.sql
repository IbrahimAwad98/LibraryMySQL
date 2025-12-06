-- ta bort befintliga användare och börja om
DROP DATABASE IF EXISTS LibraryDB;
DROP USER IF EXISTS 'Admin'@'localhost';

-- skapa ny användare och ge alla behörighet 
CREATE USER 'Admin'@'localhost' IDENTIFIED BY 'Admin2025';
GRANT ALL PRIVILEGES ON *.* TO 'Admin'@'localhost';
FLUSH PRIVILEGES;

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
(1, 8, '2024-01-15'), (1, 8, '2024-01-20'), (1, 7, '2024-01-25'),
(2, 8, '2024-02-01'), (2, 8, '2024-02-05'), (2, 7, '2024-02-10'),
(3, 10, '2024-03-01'), (3, 9, '2024-03-05'), (3, 10, '2024-03-10'),
(4, 9, '2024-04-01'), (4, 9, '2024-04-05'), (4, 8, '2024-04-10'),
(5, 10, '2024-05-01'), (5, 9, '2024-05-05'), (5, 10, '2024-05-10');

-- test för visa tabel
/*
SELECT 
    b.title,
    b.ISBN,
    b.publishedDate,
    GROUP_CONCAT(DISTINCT g.genreName SEPARATOR ', ') as genres
FROM T_Book b
LEFT JOIN T_Book_Genre bg ON b.bookID = bg.bookID
LEFT JOIN T_Genre g ON bg.genreID = g.genreID
GROUP BY b.bookID, b.title, b.ISBN, b.publishedDate;
*/ 
