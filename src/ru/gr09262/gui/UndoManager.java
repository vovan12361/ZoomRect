package ru.gr09262.gui;

import java.util.Stack;

public class UndoManager {
    private final Stack<Runnable> undoStack = new Stack<>();
    private final Stack<Runnable> redoStack = new Stack<>();
    private static final int MAX_OPERATIONS = 100;

    public void addOperation(Runnable operation) {
        if (undoStack.size() >= MAX_OPERATIONS) {
            undoStack.removeFirst();
        }
        undoStack.push(operation);
        redoStack.clear();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (canUndo()) {
            Runnable operation = undoStack.pop();
            redoStack.push(operation);
            operation.run();
        }
    }

    public void redo() {
        if (canRedo()) {
            Runnable operation = redoStack.pop();
            undoStack.push(operation);
            operation.run();
        }
    }
}

