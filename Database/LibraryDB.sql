CREATE DATABASE IF NOT EXISTS LibraryDB;
USE LibraryDB;

CREATE TABLE T_Author(
authorID INT AUTO_INCREMENT,
name VARCHAR(100) NOT NULL,
birthDate DATE,
PRIMARY KEY (authorID),
INDEX index_author_name (name) -- hantera sökning
);

CREATE TABLE T_Genre(
genreID INT AUTO_INCREMENT,
genreName VARCHAR(50) UNIQUE NOT NULL,
PRIMARY KEY (genreID),
INDEX idexx_genre_name (genreName) -- hantera sökning
);

CREATE TABLE T_Book(
bookID INT AUTO_INCREMENT,
ISBN VARCHAR(13) UNIQUE NOT NULL,
title VARCHAR(200) NOT NULL,
publishedDate DATE,
PRIMARY KEY (bookID),
-- hantera sökning
INDEX idx_book_title (title), 
INDEX idx_book_isbn (ISBN)
);


CREATE TABLE T_Rating(
ratingID INT AUTO_INCREMENT,
bookID INT NOT NULL,
ratingValue INT NOT NULL CHECK (ratingValue BETWEEN 1 AND 10),
ratingDate DATE NOT NULL,
PRIMARY KEY (ratingID),
FOREIGN KEY (bookID) REFERENCES T_Book(bookID) ON DELETE CASCADE,
INDEX idx_rating_book (bookID) -- hantera sökning
);


-- skapa koppling
CREATE TABLE T_Book_Author(
    bookID INT NOT NULL,
    authorID INT NOT NULL,
    PRIMARY KEY (bookID, authorID),  -- Composit primärnyckel
    FOREIGN KEY (bookID) REFERENCES T_Book(bookID) 
        ON DELETE CASCADE,
    FOREIGN KEY (authorID) REFERENCES T_Author(authorID) 
        ON DELETE CASCADE
);


CREATE TABLE T_Book_Genre(
    bookID INT NOT NULL,
    genreID INT NOT NULL,
    PRIMARY KEY (bookID, genreID),  -- Composit primärnyckel
    FOREIGN KEY (bookID) REFERENCES T_Book(bookID) 
        ON DELETE CASCADE,
    FOREIGN KEY (genreID) REFERENCES T_Genre(genreID) 
        ON DELETE CASCADE
);


-- fylla tabellerna!
INSERT INTO T_Author (name, birthDate) VALUES
('J.K. Rowling', '1965-07-31'),
('Terry Pratchett', '1948-04-28'),
('Neil Gaiman', '1960-11-10');

INSERT INTO T_Genre (genreName) VALUES
('Fantasy'),
('Young Adult'),
('Horror');

INSERT INTO T_Book (ISBN, title, publishedDate) VALUES
('9780439708180', 'Harry Potter and the Sorcerer''s Stone', '1997-06-26'),
('9780552152976', 'Good Omens', '1990-05-01');


INSERT INTO T_Book_Author (bookID, authorID) VALUES
(1, 1);  -- Harry Potter by J.K. Rowling


INSERT INTO T_Book_Author (bookID, authorID) VALUES
(2, 2),  -- Good Omens by Terry Pratchett
(2, 3);  -- Good Omens by Neil Gaiman

INSERT INTO T_Book_Genre (bookID, genreID) VALUES
(1, 1),  -- Harry Potter: Fantasy
(1, 2);  -- Harry Potter: Young Adult

INSERT INTO T_Book_Genre (bookID, genreID) VALUES
(2, 1),  -- Good Omens: Fantasy
(2, 3);  -- Good Omens: Horror

INSERT INTO T_Rating (bookID, ratingValue, ratingDate) VALUES
(1, 10, '2024-01-15'),
(1, 9, '2024-01-20'),
(2, 10, '2024-01-25');