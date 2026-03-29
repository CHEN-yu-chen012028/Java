import java.util.Scanner;
import java.util.ArrayList; //導入 ArrayList
public class UserLogin {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        // 使用 ArrayList 管理多個用戶物件
        ArrayList<User> users = new ArrayList<>();
        users.add(new StudentUser("Flam", "student", "123456"));
        users.add(new User("Admin", "admin", "admin888")); // 普通用戶或管理員

        try {
            System.out.print("Username: ");
            String id = sc.nextLine();
            System.out.print("Password: ");
            String pw = sc.nextLine();

            User foundUser = null;
            
            // 🎯 修正：遍歷集合進行比對
            for (User u : users) {
                if (u.getUsername().equals(id)) {
                    foundUser = u;
                    break;
                }
            }

            if (foundUser != null && foundUser.checkPassword(pw)) {
                System.out.println("Login success!");
                foundUser.showRole(); // 🎯 展現多型
            } else {
                // 🎯 修正：若沒找到或密碼錯，視為邏輯錯誤
                throw new Exception("Invalid username or password");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            sc.close();
            System.out.println("System connection closed.");
        }
    }
    static class Person {
        protected String name;
        public Person(String name) {
            this.name = name;
        }
    }

    static class User extends Person {
        private String username;
        private String password;
        public User(String name, String username, String password) {
            super(name);
            this.username = username;
            this.password = password;
        }
        public String getUsername() {
            return username;
        }
        public boolean checkPassword(String pw) {
            return password != null && password.equals(pw);
        }
        public void showRole() {
            System.out.println("User: " + name);
        }
    }

    static class StudentUser extends User {
        public StudentUser(String name, String username, String password) {
            super(name, username, password);
        }
        @Override
        public void showRole() {
            System.out.println("Student: " + name);
        }
    }
}
