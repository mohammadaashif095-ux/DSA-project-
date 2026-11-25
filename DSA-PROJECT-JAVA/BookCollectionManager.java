import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Console application for managing a personal book collection.
 *
 * Book records are stored in books.txt in the program's working directory.
 */
public class BookCollectionManager {
    private static final Path DATA_FILE = Paths.get("books.txt");
    private static final int FIRST_BOOK_NUMBER = 101;

    private final ArrayList<Book> books = new ArrayList<>();
    private int nextBookNumber = FIRST_BOOK_NUMBER;
    private final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        BookCollectionManager manager = new BookCollectionManager();
        manager.loadBooks();
        manager.runMenu();
    }

    private void runMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== Book Collection Manager =====");
            System.out.println("1. Add Book");
            System.out.println("2. Display All Books");
            System.out.println("3. Sort by Title");
            System.out.println("4. Search by Author");
            System.out.println("5. Remove Book");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    addBook();
                    break;
                case "2":
                    displayBooks();
                    break;
                case "3":
                    sortBooksByTitle();
                    break;
                case "4":
                    searchBooksByAuthor();
                    break;
                case "5":
                    removeBook();
                    break;
                case "6":
                    saveBooks();
                    System.out.println("Book records saved. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid menu choice. Please enter a number from 1 to 6.");
            }
        }
    }

    private void addBook() {
        System.out.println("\n--- Add Book ---");
        String title = readRequiredField("Enter title: ");
        String author = readRequiredField("Enter author: ");

        if (title == null || author == null) {
            System.out.println("Book was not added. Title and author must not be empty or contain '|'.");
            return;
        }

        String yearText = readYear();
        if (yearText == null) {
            return;
        }

        if (isDuplicateBook(title, author)) {
            System.out.println("A book with this title by this author already exists.");
            return;
        }

        String bookId = "BC" + nextBookNumber;
        Book book = new Book(bookId, title, author, yearText);
        books.add(book);
        nextBookNumber++;

        System.out.println("Book added successfully.");
        System.out.println(book);
    }

    private String readRequiredField(String prompt) {
        System.out.print(prompt);
        String value = input.nextLine().trim();

        if (value.isEmpty() || value.contains("|")) {
            return null;
        }
        return value;
    }

    private String readYear() {
        System.out.print("Enter year (4 digits): ");
        String year = input.nextLine().trim();

        if (!year.matches("[0-9]{4}")) {
            System.out.println("Invalid year. Enter a 4-digit number.");
            return null;
        }
        return year;
    }

    private boolean isDuplicateBook(String title, String author) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)
                    && book.getAuthor().equalsIgnoreCase(author)) {
                return true;
            }
        }
        return false;
    }

    private void displayBooks() {
        System.out.println("\n--- All Books ---");
        if (books.isEmpty()) {
            System.out.println("No books are currently in the collection.");
            return;
        }

        printTableHeader();
        for (Book book : books) {
            System.out.println(book);
        }
    }

    private void sortBooksByTitle() {
        // Bubble sort compares neighboring titles and swaps them when out of order.
        for (int pass = 0; pass < books.size() - 1; pass++) {
            for (int index = 0; index < books.size() - 1 - pass; index++) {
                String currentTitle = books.get(index).getTitle();
                String nextTitle = books.get(index + 1).getTitle();

                if (currentTitle.compareToIgnoreCase(nextTitle) > 0) {
                    Book temporary = books.get(index);
                    books.set(index, books.get(index + 1));
                    books.set(index + 1, temporary);
                }
            }
        }

        System.out.println("\nBooks sorted by title.");
        displayBooks();
    }

    private void searchBooksByAuthor() {
        System.out.print("\nEnter the author's full name to search: ");
        String authorToFind = input.nextLine().trim();

        if (authorToFind.isEmpty()) {
            System.out.println("Author name must not be empty.");
            return;
        }

        boolean found = false;
        for (Book book : books) {
            if (book.getAuthor().equalsIgnoreCase(authorToFind)) {
                if (!found) {
                    System.out.println("\n--- Matching Books ---");
                    printTableHeader();
                }
                System.out.println(book);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No books found for author: " + authorToFind);
        }
    }

    private void removeBook() {
        System.out.print("\nEnter the Book ID to remove (for example, BC101): ");
        String idToRemove = input.nextLine().trim();

        for (int index = 0; index < books.size(); index++) {
            if (books.get(index).getBookId().equalsIgnoreCase(idToRemove)) {
                Book removedBook = books.remove(index);
                System.out.println("Book removed successfully: " + removedBook);
                return;
            }
        }

        System.out.println("No book found with ID: " + idToRemove);
    }

    private void loadBooks() {
        if (!Files.exists(DATA_FILE)) {
            System.out.println("No saved collection found. Starting with an empty collection.");
            return;
        }

        int loadedCount = 0;
        int skippedCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(DATA_FILE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\|", -1);
                if (fields.length == 4
                        && !fields[0].trim().isEmpty()
                        && !fields[1].trim().isEmpty()
                        && !fields[2].trim().isEmpty()
                        && fields[3].matches("[0-9]{4}")) {
                    Book book = new Book(
                            fields[0].trim(),
                            fields[1].trim(),
                            fields[2].trim(),
                            fields[3]);
                    books.add(book);
                    updateNextBookNumber(book.getBookId());
                    loadedCount++;
                } else {
                    skippedCount++;
                }
            }
        } catch (IOException exception) {
            System.out.println("Could not load saved books: " + exception.getMessage());
            return;
        }

        System.out.println("Loaded " + loadedCount + " book(s) from " + DATA_FILE + ".");
        if (skippedCount > 0) {
            System.out.println("Warning: skipped " + skippedCount + " invalid record(s) in the data file.");
        }
    }

    private void updateNextBookNumber(String bookId) {
        if (bookId.length() > 2 && bookId.substring(0, 2).equalsIgnoreCase("BC")) {
            try {
                int bookNumber = Integer.parseInt(bookId.substring(2));
                if (bookNumber >= nextBookNumber) {
                    nextBookNumber = bookNumber + 1;
                }
            } catch (NumberFormatException exception) {
                // A non-numeric ID is retained, but it does not affect generated IDs.
            }
        }
    }

    private void saveBooks() {
        try (BufferedWriter writer = Files.newBufferedWriter(
                DATA_FILE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            for (Book book : books) {
                writer.write(book.getBookId() + "|"
                        + book.getTitle() + "|"
                        + book.getAuthor() + "|"
                        + book.getYear());
                writer.newLine();
            }
            System.out.println("Saved " + books.size() + " book(s) to " + DATA_FILE + ".");
        } catch (IOException exception) {
            System.out.println("Could not save book records: " + exception.getMessage());
        }
    }

    private void printTableHeader() {
        System.out.println("Book ID | Title | Author | Year");
        System.out.println("-----------------------------------------------");
    }
}

class Book {
    private final String bookId;
    private final String title;
    private final String author;
    private final String year;

    public Book(String bookId, String title, String author, String year) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.year = year;
    }

    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getYear() {
        return year;
    }

    @Override
    public String toString() {
        return bookId + " | " + title + " | " + author + " | " + year;
    }
}
