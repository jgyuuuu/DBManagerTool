package com.dbtool.command;

public class CommandParser {

    public static CommandResult parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new CommandResult(CommandType.EMPTY, "");
        }

        String trimmed = input.trim();

        // 检查是否是元命令（以 \ 开头）
        if (trimmed.startsWith("\\")) {
            return parseMetaCommand(trimmed);
        }

        // 检查是否是内置命令
        CommandType builtIn = parseBuiltInCommand(trimmed.toLowerCase());
        if (builtIn != CommandType.UNKNOWN) {
            return new CommandResult(builtIn, "");
        }

        // 否则是SQL命令
        return new CommandResult(CommandType.SQL, trimmed);
    }

    private static CommandResult parseMetaCommand(String input) {
        String[] parts = input.substring(1).split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "l":
            case "list":
                return new CommandResult(CommandType.LIST_DATABASES, argument);

            case "t":
            case "tables":
                return new CommandResult(CommandType.LIST_TABLES, argument);

            case "d":
            case "desc":
            case "describe":
                if (argument.isEmpty()) {
                    return new CommandResult(CommandType.ERROR, "Table name required for describe command");
                }
                return new CommandResult(CommandType.DESCRIBE_TABLE, argument);

            case "info":
                return new CommandResult(CommandType.DATABASE_INFO, argument);

            case "status":
                return new CommandResult(CommandType.TABLE_STATUS, argument);

            case "use":
                if (argument.isEmpty()) {
                    return new CommandResult(CommandType.ERROR, "Database name required for use command");
                }
                return new CommandResult(CommandType.USE_DATABASE, argument);

            case "help":
                return new CommandResult(CommandType.HELP, argument);

            case "history":
                return new CommandResult(CommandType.HISTORY, argument);

            case "clear_history":
                return new CommandResult(CommandType.CLEAR_HISTORY, argument);

            case "export":
                if (argument.isEmpty()) {
                    return new CommandResult(CommandType.ERROR, "Filename required for export command");
                }
                return new CommandResult(CommandType.EXPORT_CSV, argument);

            case "export_csv":
                if (argument.isEmpty()) {
                    return new CommandResult(CommandType.ERROR, "Filename required for export command");
                }
                return new CommandResult(CommandType.EXPORT_CSV, argument);

            case "export_txt":
                if (argument.isEmpty()) {
                    return new CommandResult(CommandType.ERROR, "Filename required for export command");
                }
                return new CommandResult(CommandType.EXPORT_TEXT, argument);

            default:
                return new CommandResult(CommandType.ERROR, "Unknown meta command: " + command);
        }
    }

    private static CommandType parseBuiltInCommand(String command) {
        switch (command) {
            case "exit":
            case "quit":
                return CommandType.EXIT;

            case "help":
                return CommandType.HELP;

            case "status":
                return CommandType.STATUS;

            case "test":
                return CommandType.TEST_CONNECTION;

            case "config":
                return CommandType.SHOW_CONFIG;


            default:
                return CommandType.UNKNOWN;
        }
    }

    public static class CommandResult {
        private CommandType type;
        private String content;

        public CommandResult(CommandType type, String content) {
            this.type = type;
            this.content = content;
        }

        public CommandType getType() { return type; }
        public String getContent() { return content; }
    }

    public enum CommandType {
        SQL,
        LIST_DATABASES,
        LIST_TABLES,
        DESCRIBE_TABLE,
        DATABASE_INFO,
        TABLE_STATUS,
        USE_DATABASE,
        HELP,
        STATUS,
        TEST_CONNECTION,
        SHOW_CONFIG,
        EXIT,
        EMPTY,
        ERROR,
        HISTORY,           // 显示历史记录
        CLEAR_HISTORY,     // 清除历史记录
        EXPORT_CSV,        // 导出为CSV
        EXPORT_TEXT,       // 导出为文本
        PAGINATION,        // 分页命令
        UNKNOWN
    }
}