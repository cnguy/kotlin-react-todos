package todolist

import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import todolist.types.Task

enum class TodoFilter(val prettyName: String) {
    ALL("All"), ACTIVE("Active"), COMPLETED("Completed")
}

interface TodoListState : RState {
    var todos: List<Task>
    var isAllChecked: Boolean
    var filter: TodoFilter

    companion object {
        private fun todosToggled(s: TodoListState): List<Task> = s.todos.map { it.copy(isComplete = !it.isComplete) }
        private fun todosAllCompleted(s: TodoListState): List<Task> = s.todos.map { it.copy(isComplete = true) }
        private fun isEitherAllCompletedOrAllNot(s: TodoListState): Boolean = s.todos.all { it.isComplete } || s.todos.all { !it.isComplete }
        fun getTodosBasedOnState(s: TodoListState): List<Task> = if (isEitherAllCompletedOrAllNot(s)) {
            todosToggled(s)
        } else {
            todosAllCompleted(s)
        }
    }
}

class TodoList(props: RProps) : RComponent<RProps, TodoListState>(props) {
    override fun TodoListState.init(props: RProps) {
        todos = listOf()
        isAllChecked = false
        filter = TodoFilter.ALL
    }

    override fun RBuilder.render() {
        section("todoapp") {
            header("header") {
                h1 { +"todos" }
                todoNewBar(::handleAddTodo)
            }
            section("main") {
                if (state.todos.isNotEmpty()) {
                    input(
                            type = InputType.checkBox,
                            classes = "toggle-all"
                    ) {
                        attrs.checked = state.isAllChecked
                        attrs.onChangeFunction = { toggleAll() }
                    }
                }
                ul("todo-list") {
                    state.todos.
                            filter {
                                when (state.filter) {
                                    TodoFilter.ALL -> true
                                    TodoFilter.ACTIVE -> !it.isComplete
                                    TodoFilter.COMPLETED -> it.isComplete
                                }
                            }
                            .map {
                                todoItem(it, ::handleCheckTodoItem, ::handleEditTodoItem, ::handleDeleteTodoItem)
                            }
                }
            }
            if (state.todos.isNotEmpty()) {
                footer("footer") {
                    span("todo-count") {
                        val activeCount = state.todos.fold(0) { total: Int, task: Task ->
                            if (task.isComplete) total else total + 1
                        }
                        +(when (activeCount) {
                            0 -> "All items completed"
                            1 -> "1 item left"
                            else -> "$activeCount items left"
                        })
                    }
                    ul("filters") {
                        enumValues<TodoFilter>().map {
                            li {
                                a(href = "#", classes = if (it == state.filter) "selected" else "") {
                                    +it.prettyName
                                    attrs.onClickFunction = handleFilterChange(it)
                                }
                            }
                        }
                    }
                    if (state.todos.any { it.isComplete }) {
                        button(classes = "clear-completed") {
                            +"Clear completed"
                            attrs.onClickFunction = { setState { todos = todos.filter { !it.isComplete } } }
                        }
                    }
                }
            }
        }
    }

    private fun handleAddTodo(taskDescription: String) {
        setState {
            todos = state.todos + Task(taskDescription)
            isAllChecked = false
        }
    }

    private fun toggleAll() {
        setState {
            todos = TodoListState.getTodosBasedOnState(state)
            isAllChecked = !state.isAllChecked
        }
    }

    private fun handleCheckTodoItem(task: Task) {
        val newTodos = state.todos.map {
            if (it === task) task.copy(isComplete = !task.isComplete) else it
        }
        setState {
            todos = newTodos
            isAllChecked = newTodos.all { it.isComplete }
        }
    }

    private fun handleEditTodoItem(task: Task, editValue: String) {
        val newTodos = state.todos.map {
            if (it === task) task.copy(description = editValue) else it
        }
        setState {
            todos = newTodos
        }
    }

    private fun handleDeleteTodoItem(task: Task) {
        setState { todos = state.todos.filter { it !== task } }
    }

    private fun handleFilterChange(type: TodoFilter): (Event) -> Unit {
        return fun(_: Event) {
            setState { filter = type }
        }
    }
}

fun RBuilder.todoList() = child(TodoList::class) {}