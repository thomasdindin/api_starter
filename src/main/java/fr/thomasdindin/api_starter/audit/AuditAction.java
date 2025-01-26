package fr.thomasdindin.api_starter.audit;

public enum AuditAction {
    SUCCESSFUL_REGISTRATION,
    FAILED_REGISTRATION,
    SUCCESSFUL_LOGIN,
    FAILED_LOGIN,
    BLOCKED_ACCOUNT,
    LOGOUT,
    RESET_PASSWORD,
}