package ui;

import dao.CategoryDao;
import Entity.Category;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.table.DefaultTableCellRenderer;

public class ManagecategoriesUI extends JFrame {
    private CategoryDao categoryDao;
    private JTable categoriesTable;
    private DefaultTableModel tableModel;
    private Connection connection; 
    private JTextField searchField;

    public ManagecategoriesUI(Connection connection) {
        this.connection = connection; 
        this.categoryDao = new CategoryDao(connection);

        setTitle("Manage Categories");
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 222, 179)); 

        // Create search bar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(new Color(245, 222, 179));
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(600, searchField.getPreferredSize().height)); 
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(188, 152, 126));
        searchButton.setOpaque(true);
        searchButton.setBorderPainted(false);
        searchButton.addActionListener(e -> searchCategories());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        panel.add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Actions"}, 0);
        categoriesTable = new JTable(tableModel);
        categoriesTable.setRowHeight(30);
        categoriesTable.getTableHeader().setBackground(new Color(209, 190, 168));
        categoriesTable.getTableHeader().setForeground(Color.BLACK); 
        categoriesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14)); 
        categoriesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                component.setBackground(new Color(227, 218, 201));
                return component;
            }
        });
        categoriesTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        categoriesTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

        loadCategories();

        JScrollPane scrollPane = new JScrollPane(categoriesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        JButton addCategoryButton = new JButton("Add Category");
        addCategoryButton.setBackground(new Color(188, 152, 126));
        addCategoryButton.setOpaque(true);
        addCategoryButton.setBorderPainted(false);
        addCategoryButton.addActionListener(e -> openAddCategoryUI());

        JButton backButton = new JButton("Back");
        backButton.setBackground(new Color(188, 152, 126)); 
        backButton.setOpaque(true);
        backButton.setBorderPainted(false);
        backButton.addActionListener(e -> {
            new DashboardUI().setVisible(true);
            this.dispose(); // Close the current window
        });

        buttonPanel.add(backButton);
        buttonPanel.add(addCategoryButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryDao.getAllCategories();
            String searchText = searchField.getText().toLowerCase();
            List<Category> filteredCategories = categories.stream()
                    .filter(category -> category.getName().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());

            for (Category category : filteredCategories) {
                Object[] rowData = new Object[]{
                        category.getId(),
                        category.getName(),
                        "Actions" 
                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchCategories() {
        tableModel.setRowCount(0); // Clear existing rows
        loadCategories();
    }

    private void openAddCategoryUI() {
        String categoryName = JOptionPane.showInputDialog(this, "Enter category name:");
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            try {
                categoryDao.addCategory(categoryName.trim());
                reloadCategories();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error adding category: " + e.getMessage());
            }
        }
    }

    private void openUpdateCategoryUI(Category category) {
        String newName = JOptionPane.showInputDialog(this, "Enter new name for category:", category.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            try {
                categoryDao.updateCategory(category.getId(), newName.trim());
                reloadCategories();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error updating category: " + e.getMessage());
            }
        }
    }

    private void deleteCategory(Category category) {
        try {
            categoryDao.deleteCategory(category.getId());
            reloadCategories();
            JOptionPane.showMessageDialog(this, "Category deleted successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting category: " + e.getMessage());
        }
    }

    private void reloadCategories() {
        tableModel.setRowCount(0); 
        loadCategories();
    }

    // ButtonRenderer class
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton updateButton;
        private JButton deleteButton;

        public ButtonRenderer() {
            setLayout(new FlowLayout());
            setBackground(new Color(227, 218, 201));

            updateButton = new JButton("Update");
            updateButton.setBackground(Color.GREEN);
            updateButton.setOpaque(true);
            updateButton.setBorderPainted(false);

            deleteButton = new JButton("Delete");
            deleteButton.setBackground(Color.RED);
            deleteButton.setOpaque(true);
            deleteButton.setBorderPainted(false);

            add(updateButton);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // ButtonEditor class
    class ButtonEditor extends DefaultCellEditor {
        private JButton updateButton;
        private JButton deleteButton;
        private JPanel panel;
        private Category currentCategory;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout());
            panel.setBackground(new Color(227, 218, 201));

            updateButton = new JButton("Update");
            updateButton.setBackground(Color.GREEN); 
            updateButton.setOpaque(true);
            updateButton.setBorderPainted(false);

            deleteButton = new JButton("Delete");
            deleteButton.setBackground(Color.RED);
            deleteButton.setOpaque(true);
            deleteButton.setBorderPainted(false);

            updateButton.addActionListener(e -> {
                openUpdateCategoryUI(currentCategory);
                reloadCategories(); 
                fireEditingStopped();
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                deleteCategory(currentCategory);
                reloadCategories(); 
            });

            panel.add(updateButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            try {
                List<Category> categories = categoryDao.getAllCategories();
                currentCategory = categories.get(row);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentCategory;
        }
    }

    public static void main(String[] args) {
        Connection connection = createDatabaseConnection();
        SwingUtilities.invokeLater(() -> new ManagecategoriesUI(connection).setVisible(true));
    }


    private static Connection createDatabaseConnection() {
      
        return null; 
    }
}
