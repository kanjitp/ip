package dialog;

import command.InvalidTimeFormatException;

import parser.Parser;
import task.Task;
import task.TimeTask;
import task.TaskList;
import ui.ChatPage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TaskDialog is a class for displaying dialogs which also has TaskList as its component
 *
 * @author Kan Jitpakdi
 * @author GitHub: kanjitp
 * @version 0.02
 * @since 0.01
 */
public class TaskDialog extends Dialog {
    // This class is a child of dialog class which allow the user to interact with the task
    private ChatPage chatPage;
    private TaskList taskList;


    /** private constructor only to be used within the class */
    private TaskDialog(ChatPage chatPage, ArrayList<String> sentences, TaskList taskList) {
        super(sentences);
        this.chatPage = chatPage;
        this.taskList = taskList;
    }

    private TaskDialog(ArrayList<String> sentences, TaskList taskList) {
        super(sentences);
        this.chatPage = chatPage;
        this.taskList = taskList;
    }

    public ChatPage getChatPage() {
        return this.chatPage;
    }

    public void setChatPage(ChatPage chatPage) {
        this.chatPage = chatPage;
    }

    /**
     * Factory method for creating TaskDialog as Dialog type (to be cast by developer)
     * Factory method from TaskDialog allow the user to fetch the existing Dialog in the archive.
     * The default TaskList will have an empty ArrayList of task.
     *
     * @param id the id of the TaskDialog
     * @return TaskDialog of type Dialog
     */
    public static Dialog generate(String id) {
        if (archive.containsKey(id)) {
            return archive.get(id);
        } else {
            final Dialog newDialog = new TaskDialog(new ArrayList<>(), new TaskList(new ArrayList<>()));
            archive.put(id, newDialog);
            return newDialog;
        }
    }

    /**
     * Factory method for creating TaskDialog as Dialog type (to be cast by developer)
     * Factory method from TaskDialog allow the user to fetch the existing Dialog in the archive.
     *
     * @param id       the id of the TaskDialog
     * @param taskList the taskList the TaskDialog is going to have
     * @return TaskDialog of type Dialog
     */
    public static Dialog generate(String id, TaskList taskList) {
        if (archive.containsKey(id)) {
            return archive.get(id);
        } else {
            final Dialog newDialog = new TaskDialog(new ArrayList<>(), taskList);
            archive.put(id, newDialog);
            return newDialog;
        }
    }

    /**
     * Add the task to the TaskList of this Dialog and print out feedback to the user
     *
     * @param task the task to be added to the TaskList of this TaskDialog
     * @throws DialogException dialog cannot have the same id while the app is running
     */
    public void addTask(Task task) throws DialogException {
        Dialog addDialog = Dialog.generate(Integer.toString(task.hashCode()));
        taskList.add(task);
        addDialog.add("Got it. I've added this task:");
        addDialog.add("  " + task);
        addDialog.add("Now you have " + this.taskList.length() + " tasks in the list.");
        chatPage.printAlicely(addDialog.toString());
    }

    /**
     * Return TaskDialog filtered by the specific deadline of format yyyy-MM-dd
     *
     * @param deadline the deadline of format yyyy-MM-dd
     * @return TaskDialog with new TaskList stored in it
     * @throws InvalidTimeFormatException time of illegal format (not yyyy-MM-dd)
     */
    public TaskDialog getFromDeadline(String deadline) throws InvalidTimeFormatException {
        LocalDate dlDate = Parser.parseTimeString(deadline);
        return new TaskDialog(new ArrayList<>(List.of("task.Deadline: "
                + dlDate.format(DateTimeFormatter.ofPattern("MMM d yyyy")))),
                new TaskList(new ArrayList<>(this.taskList.getTasks().stream().filter((task) -> {
                    if (!task.getClass().getSuperclass().getName().equals("task.TimeTask")) {
                        // return any task without time associated with it
                        return true;
                    } else {
                        TimeTask timetask = (TimeTask) task;
                        return timetask.getTime().isBefore(dlDate) || timetask.getTime().isEqual(dlDate);
                    }
                }).collect(Collectors.toList()))));
    }

    /**
     * Return TaskDialog filtered by the keyword given by the user
     *
     * @param keyword the keyword to search description
     * @return TaskDialog with TaskList with tasks matching the keyword
     */
    public TaskDialog getFromKeyword(String keyword) {
        return new TaskDialog(new ArrayList<>(List.of("Find: " + keyword)),
                new TaskList(new ArrayList<>(this.taskList.getTasks().stream().filter(
                        (task) -> task.getDescription().contains(keyword)).collect(Collectors.toList()))));
    }

    /**
     * Return the TaskList of current TaskDialog
     *
     * @return the taskList stored in this TaskDialog
     */
    public TaskList getTaskList() {
        return this.taskList;
    }

    /**
     * Mark the task in TaskList with the specified index and print feedback to the user
     *
     * @param index the index of the Task in TaskList in this TaskDialog to mark as done
     * @throws IndexOutOfBoundsException illegal index
     * @throws DialogException           dialog cannot have the same id while the app is running
     */
    public void markTaskAsDone(int index) throws IndexOutOfBoundsException, DialogException {
        if (index < 0 || index > this.taskList.length()) {
            throw new IndexOutOfBoundsException();
        } else {
            Task task = taskList.get(index);
            task.markAsDone();
            String id = "markAsDone" + task.hashCode();
            Dialog markAsDoneDialog = Dialog.generate(id);
            markAsDoneDialog.add("Nice! I've marked this task as done:");
            markAsDoneDialog.add("  " + task);
            chatPage.printAlicely(markAsDoneDialog.toString());

            // allow duplicates later
            Dialog.archive.remove(id);
        }
    }

    /**
     * Delete the task by index and print feedback to the user
     *
     * @param index the index of the task to be deleted in TaskList of this TaskDialog
     * @throws IndexOutOfBoundsException illegal index
     * @throws DialogException           dialog cannot have the same id while the app is running
     */
    public void deleteTaskByIndex(int index) throws IndexOutOfBoundsException, DialogException {
        if (index < 0 || index > this.taskList.length()) {
            throw new IndexOutOfBoundsException();
        } else {
            Task task = taskList.get(index);
            taskList.remove(index);
            String id = "remove" + task.hashCode();
            Dialog removeDialog = Dialog.generate(id);
            removeDialog.add("Noted. I've removed this task:");
            removeDialog.add("  " + task);
            removeDialog.add("Now you have " + this.taskList.length() + " tasks in the list.");
            chatPage.printAlicely(removeDialog.toString());
            Dialog.archive.remove(id);
            Dialog.archive.remove(task.toString());
        }
    }


    /**
     * Return the string representation of the TaskDialog with its sentences at the top
     * and followed by the TaskList
     *
     * @return string representation of taskDialog
     */
    @Override
    public String toString() {
        String dialogs = this.sentences.stream().reduce("", (s1, s2) -> s1 + s2 + "\n");

        return dialogs + "Here are the tasks in your list:\n" + taskList;
    }
}
