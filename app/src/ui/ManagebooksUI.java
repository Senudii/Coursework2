package ui;

import dao.BookDao;
import Entity.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ManagebooksUI extends JFrame {
    private BookDao bookDao;
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private Connection connection;
    private JTextField searchField;

    public ManagebooksUI(Connection connection) {
        this.connection = connection;
        this.bookDao = new BookDao(connection);

        setTitle("Manage Books");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 222, 179)); 

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(new Color(245, 222, 179));
        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(188, 152, 126));
        searchButton.addActionListener(e -> searchBooks());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        panel.add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Title", "Author", "Category", "Edition", "Description", "Actions"}, 0);
        booksTable = new JTable(tableModel);
        booksTable.setRowHeight(40);

        booksTable.getTableHeader().setBackground(new Color(209, 190, 168));
        booksTable.getTableHeader().setForeground(Color.BLACK);
        booksTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        booksTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                component.setBackground(new Color(227, 218, 201));
                return component;
            }
        });

        booksTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        booksTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

        loadBooks();

        JScrollPane scrollPane = new JScrollPane(booksTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton addBookButton = new JButton("Add Book");
        addBookButton.setBackground(new Color(188, 152, 126));
        addBookButton.setOpaque(true);
        addBookButton.setBorderPainted(false);
        addBookButton.addActionListener(e -> openAddBookUI());

        JButton backButton = new JButton("Back");
        backButton.setBackground(new Color(188, 152, 126)); 
        backButton.setOpaque(true);
        backButton.setBorderPainted(false);
        backButton.addActionListener(e -> {
            new DashboardUI().setVisible(true);
            this.dispose();
        });

        buttonPanel.add(backButton);
        buttonPanel.add(addBookButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void loadBooks() {
        try {
            List<Book> books = bookDao.getAllBooks();
            for (Book book : books) {
                Object[] rowData = new Object[]{
                        book.getTitle(),
                        book.getAuthor(),
                        book.getCategoryId(),
                        book.getEdition(),
                        book.getDescription(),
                        "Delete"
                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchBooks() {
        String searchText = searchField.getText().toLowerCase();
        try {
            List<Book> books = bookDao.getAllBooks();
            List<Book> filteredBooks = books.stream()
                    .filter(book -> book.getTitle().toLowerCase().contains(searchText) ||
                                    book.getAuthor().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());

            tableModel.setRowCount(0);
            for (Book book : filteredBooks) {
                Object[] rowData = new Object[]{
                        book.getTitle(),
                        book.getAuthor(),
                    
                        book.getEdition(),
                        book.getDescription(),
                        "Delete"
                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    

    private void openAddBookUI() {
        AddBookUI addBookUI = new AddBookUI(this, connection);
        addBookUI.setVisible(true);
        reloadBooks();
    }

    private void deleteBook(Book book) {
        try {
            bookDao.deleteBook(book.getId());
            reloadBooks();
            JOptionPane.showMessageDialog(this, "Book deleted successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting book: " + e.getMessage());
        }
    }

    private void reloadBooks() {
        tableModel.setRowCount(0); 
        loadBooks();
    }

    // ButtonRenderer class
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton deleteButton;

        public ButtonRenderer() {
            setLayout(new FlowLayout());
            setBackground(new Color(227, 218, 201)); 

            deleteButton = new JButton("Delete");
            deleteButton.setBackground(Color.RED);
            deleteButton.setOpaque(true);
            deleteButton.setBorderPainted(false);

            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // ButtonEditor class
    class ButtonEditor extends DefaultCellEditor {
        private JButton deleteButton;
        private JPanel panel;
        private Book currentBook;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout());
            panel.setBackground(new Color(227, 218, 201)); 

            deleteButton = new JButton("Delete");
            deleteButton.setBackground(Color.RED); 
            deleteButton.setOpaque(true);
            deleteButton.setBorderPainted(false);

            deleteButton.addActionListener(e -> {
                deleteBook(currentBook);
                reloadBooks();
                fireEditingStopped();
            });

            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            try {
                List<Book> books = bookDao.getAllBooks();
                currentBook = books.get(row);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentBook;
        }
    }

    public static void main(String[] args) {
        Connection connection = createDatabaseConnection();
        SwingUtilities.invokeLater(() -> new ManagebooksUI(connection).setVisible(true));
    }

    private static Connection createDatabaseConnection() {
        return null; 
    }
}
