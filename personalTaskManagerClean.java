import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersonalTaskManagerClean {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<String> PRIORITIES = Arrays.asList("Thấp", "Trung bình", "Cao");

    // Load tasks từ file
    private JSONArray loadTasks() {
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = new JSONParser().parse(reader);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            }
        } catch (IOException | ParseException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
        }
        return new JSONArray();
    }

    // Lưu tasks vào file
    private void saveTasks(JSONArray tasks) {
        try (FileWriter file = new FileWriter(DB_FILE_PATH)) {
            file.write(tasks.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
        }
    }

    // Kiểm tra dữ liệu hợp lệ
    private boolean isValid(String title, String dueDateStr, String priorityLevel) {
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Lỗi: Tiêu đề không được để trống.");
            return false;
        }

        try {
            LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (Exception e) {
            System.out.println("Lỗi: Ngày đến hạn không hợp lệ.");
            return false;
        }

        if (!PRIORITIES.contains(priorityLevel)) {
            System.out.println("Lỗi: Mức độ ưu tiên không hợp lệ.");
            return false;
        }

        return true;
    }

    // Thêm nhiệm vụ mới
    public JSONObject addTask(String title, String description, String dueDateStr, String priority) {
        if (!isValid(title, dueDateStr, priority)) return null;

        JSONArray tasks = loadTasks();
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            if (task.get("title").equals(title) && task.get("due_date").equals(dueDateStr)) {
                System.out.println("Lỗi: Nhiệm vụ trùng lặp.");
                return null;
            }
        }

        JSONObject newTask = new JSONObject();
        newTask.put("id", UUID.randomUUID().toString());
        newTask.put("title", title);
        newTask.put("description", description);
        newTask.put("due_date", dueDateStr);
        newTask.put("priority", priority);
        newTask.put("status", "Chưa hoàn thành");
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        newTask.put("created_at", now);
        newTask.put("last_updated_at", now);

        tasks.add(newTask);
        saveTasks(tasks);

        System.out.println("✅ Đã thêm nhiệm vụ: " + title);
        return newTask;
    }

    public static void main(String[] args) {
        PersonalTaskManagerClean manager = new PersonalTaskManagerClean();

        manager.addTask("Học bài", "Ôn thi công nghệ phần mềm", "2025-07-16", "Cao");
        manager.addTask("Học bài", "Ôn thi công nghệ phần mềm", "2025-07-16", "Cao"); // Trùng
        manager.addTask("", "Thiếu tiêu đề", "2025-07-18", "Thấp");
        manager.addTask("Tập gym", "Tập thể dục mỗi sáng", "2025-07-17", "Trung bình");
    }
}
