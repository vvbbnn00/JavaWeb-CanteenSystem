package cn.vvbbnn00.canteen.filter.role_check;

import cn.vvbbnn00.canteen.model.User;

public class RoleCheckFilter {

    /**
     * Checks if the user has the required role.
     *
     * @param user          the user object
     * @param requiredRole  the required role string
     * @return 1 if the user has the required role, 0 if not, -1 if the user is null
     */
    public static int checkRole(User user, String requiredRole) {
        if (user == null) {
            return -1;
        }
        boolean checkPass = false;
        String userRole = user.getRole().toString();
        switch (requiredRole) {
            case "admin":
                if (userRole.equals(User.Role.admin.toString())) {
                    checkPass = true;
                }
                break;
            case "canteen_admin":
                if (userRole.equals(User.Role.canteen_admin.toString())) {
                    checkPass = true;
                }
                if (userRole.equals(User.Role.admin.toString())) {
                    checkPass = true;
                }
                break;
            default:
                checkPass = true;
                break;
        }
        return checkPass ? 1 : 0;
    }
}
