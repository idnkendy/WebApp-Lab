/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author Admin
 */
import dao.StudentDAO;
import model.Student;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import java.io.File;
import java.nio.file.Paths;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "StudentController", urlPatterns = {"/student"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class StudentController extends HttpServlet {

    private StudentDAO studentDAO;

    @Override
    public void init() {
        studentDAO = new StudentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "new":
                showNewForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteStudent(request, response);
                break;
            case "search":
            case "sort":
            case "filter":
            case "list":
            default:
                try {
                    listStudents(request, response);
                } catch (SQLException ex) {
                    // SỬA LỖI MÀN HÌNH TRẮNG:
                    // Nếu lỗi DB, in lỗi ra console và hiển thị thông báo lên trang list
                    ex.printStackTrace();
                    request.setAttribute("error", "Database Error: " + ex.getMessage());
                    // Vẫn forward về trang list để người dùng thấy lỗi thay vì trang trắng
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
                    dispatcher.forward(request, response);
                }
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) action = "list"; // Phòng hờ null

        switch (action) {
            case "insert":
                insertStudent(request, response);
                break;
            case "update":
                updateStudent(request, response);
                break;
            default:
                response.sendRedirect("student?action=list");
                break;
        }
    }

    // List all students (Bao gồm cả Search, Filter, Sort)
    private void listStudents(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        // --- 1. Thu thập tham số ---
        String pageParam = request.getParameter("page");
        String sortBy = studentDAO.validateSortBy(request.getParameter("sortBy"));
        String order = studentDAO.validateOrder(request.getParameter("order"));
        String major = request.getParameter("major");
        String keyword = (request.getParameter("keyword") == null) ? "" : request.getParameter("keyword").trim();

        int recordsPerPage = 10;
        int currentPage = 1;
        int totalRecords = 0;
        List<Student> listStudent = new ArrayList<>();

        // Lấy trang hiện tại
        if (pageParam != null) {
            try {
                currentPage = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                currentPage = 1;
            }
        }

        // --- 2. Xử lý Logic ---
        boolean isSearching = !keyword.isEmpty();
        boolean isFiltering = (major != null && !major.isEmpty());
        
        if (isSearching) {
            // CASE 1: Tìm kiếm
            listStudent = studentDAO.searchStudents(keyword);
            totalRecords = listStudent.size();
            currentPage = 1;
        } else if (isFiltering) {
            // CASE 2: Lọc theo ngành
            listStudent = studentDAO.getStudentsByMajor(major);
            totalRecords = listStudent.size();
            currentPage = 1;
        } else {
            // CASE 3: Mặc định (Phân trang + Sắp xếp)
            totalRecords = studentDAO.getTotalStudents();
            int totalPages = (int) Math.ceil((double) totalRecords / recordsPerPage);

            if (totalPages > 0 && currentPage > totalPages) {
                currentPage = totalPages;
            } else if (totalPages == 0) {
                currentPage = 1;
            }

            int offset = (currentPage - 1) * recordsPerPage;
            // Đảm bảo offset không âm
            if (offset < 0) offset = 0;
            
            listStudent = studentDAO.getStudentsPaginatedAndSorted(sortBy, order, offset, recordsPerPage); 
            
            request.setAttribute("totalPages", totalPages);
        }
        
        // --- 3. Đặt thuộc tính cho JSP ---
        request.setAttribute("listStudent", listStudent); // Tên biến này phải khớp với items="${listStudent}" bên JSP
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("sortBy", sortBy);
        request.setAttribute("order", order);
        request.setAttribute("selectedMajor", major);
        request.setAttribute("keyword", keyword);

        // --- 4. Chuyển tiếp (QUAN TRỌNG: Đường dẫn phải có /views/) ---
        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
        dispatcher.forward(request, response);
    }
    
    // Show form for new student
    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("student", null);
        // QUAN TRỌNG: Đường dẫn phải có /views/
        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
        dispatcher.forward(request, response);
    }

    // Show form for editing student
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        Student existingStudent = studentDAO.getStudentById(id);

        request.setAttribute("student", existingStudent);
        // QUAN TRỌNG: Đường dẫn phải có /views/
        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
        dispatcher.forward(request, response);
    }

    // Insert new student
    private void insertStudent(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String studentCode = request.getParameter("studentCode");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String major = request.getParameter("major");
        String photoFileName = handleFileUpload(request);

        Student newStudent = new Student(studentCode, fullName, email, major);
        newStudent.setPhoto(photoFileName);
        
        if (!validateStudent(newStudent, request)) {
            request.setAttribute("student", newStudent);
            // QUAN TRỌNG: Đường dẫn phải có /views/
            RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
            dispatcher.forward(request, response);
            return;
        }

        if (studentDAO.addStudent(newStudent)) {
            response.sendRedirect("student?action=list&message=Student added successfully");
        } else {
            response.sendRedirect("student?action=list&error=Failed to add student");
        }
    }

    // Update student
    private void updateStudent(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        String studentCode = request.getParameter("studentCode");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String major = request.getParameter("major");
        
        String photoFileName = handleFileUpload(request);
        
        if (photoFileName == null) {
            photoFileName = request.getParameter("currentPhoto"); // Lấy từ input hidden
        }

        Student student = new Student(studentCode, fullName, email, major);
        student.setId(id);
        student.setPhoto(photoFileName);

        if (!validateStudent(student, request)) {
            request.setAttribute("student", student);
            // QUAN TRỌNG: Đường dẫn phải có /views/
            RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
            dispatcher.forward(request, response);
            return;
        }

        if (studentDAO.updateStudent(student)) {
            response.sendRedirect("student?action=list&message=Student updated successfully");
        } else {
            response.sendRedirect("student?action=list&error=Failed to update student");
        }
    }

    // Delete student
    private void deleteStudent(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        if (studentDAO.deleteStudent(id)) {
            response.sendRedirect("student?action=list&message=Student deleted successfully");
        } else {
            response.sendRedirect("student?action=list&error=Failed to delete student");
        }
    }

    // --- Validate helper ---
    private boolean validateStudent(Student student, HttpServletRequest request) {
        boolean isValid = true;
        String codePattern = "[A-Z]{2}[0-9]{3,}";
        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";

        if (student.getStudentCode() == null || student.getStudentCode().trim().isEmpty()) {
            request.setAttribute("errorCode", "Student Code is required.");
            isValid = false;
        } else if (!student.getStudentCode().trim().matches(codePattern)) {
            request.setAttribute("errorCode", "Invalid format. Use 2 uppercase letters + 3 or more digits (e.g., SV001).");
            isValid = false;
        }

        if (student.getFullName() == null || student.getFullName().trim().isEmpty()) {
            request.setAttribute("errorName", "Full Name is required.");
            isValid = false;
        } else if (student.getFullName().trim().length() < 2) {
            request.setAttribute("errorName", "Full Name must be at least 2 characters long.");
            isValid = false;
        }

        if (student.getEmail() != null && !student.getEmail().trim().isEmpty()) {
            if (!student.getEmail().trim().matches(emailPattern)) {
                request.setAttribute("errorEmail", "Invalid email format.");
                isValid = false;
            }
        }

        if (student.getMajor() == null || student.getMajor().trim().isEmpty()) {
            request.setAttribute("errorMajor", "Major is required.");
            isValid = false;
        }

        return isValid;
    }
   
    // --- File Upload helper ---
    private String handleFileUpload(HttpServletRequest request) throws IOException, ServletException {
        Part filePart = request.getPart("photo"); 
        if (filePart == null || filePart.getSize() == 0) {
            return null; 
        }

        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) extension = fileName.substring(i).toLowerCase();
        
        if (!extension.equals(".jpg") && !extension.equals(".jpeg") && !extension.equals(".png")) {
            return null;
        }

        String uniqueFileName = System.currentTimeMillis() + extension;
        String uploadPath = request.getServletContext().getRealPath("") + File.separator + "uploads";
        
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdir();

        filePart.write(uploadPath + File.separator + uniqueFileName);
        return uniqueFileName;
    }
}