package ru.gr09262.gui;

import java.util.Stack;

/**
 * Класс для управления операциями "Отмена" (Undo) и "Повтор" (Redo).
 * Позволяет хранить операции, которые можно отменить или повторить,
 * с ограничением на максимальное количество сохраняемых операций.
 */
public class UndoManager {
    private final Stack<Runnable> undoStack = new Stack<>();
    private final Stack<Runnable> redoStack = new Stack<>();
    private static final int MAX_OPERATIONS = 100;

    /**
     * Добавляет новую операцию в стек отмены.
     * Очищает стек повтора при добавлении новой операции.
     *
     * @param operation Операция, которую можно будет отменить.
     */
    public void addOperation(Runnable operation) {
        if (undoStack.size() >= MAX_OPERATIONS) {
            undoStack.removeFirst();
        }
        undoStack.push(operation);
        redoStack.clear();
    }

    /**
     * Проверяет, доступна ли операция "Отмена".
     *
     * @return true, если есть операции для отмены; false, если стек отмены пуст.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Проверяет, доступна ли операция "Повтор".
     *
     * @return true, если есть операции для повтора; false, если стек повтора пуст.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Выполняет операцию "Отмена".
     * Переносит отменённую операцию из стека отмены в стек повтора и вызывает её выполнение.
     */
    public void undo() {
        if (canUndo()) {
            Runnable operation = undoStack.pop();
            redoStack.push(operation);
            operation.run();
        }
    }

    /**
     * Выполняет операцию "Повтор".
     * Переносит повторённую операцию из стека повтора в стек отмены и вызывает её выполнение.
     */
    public void redo() {
        if (canRedo()) {
            Runnable operation = redoStack.pop();
            undoStack.push(operation);
            operation.run();
        }
    }
}

