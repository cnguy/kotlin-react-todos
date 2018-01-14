package todolist

import kotlinx.html.InputType
import kotlinx.html.js.*
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.*
import react.dom.*
import todolist.types.Task

val ENTER_KEY_CODE = 13
val ESCAPE_KEY_CODE = 27

interface TodoItemProps : RProps {
    var task: Task
    var onCheck: (Task) -> Unit
    var onEdit: (Task, String) -> Unit
    var onDelete: (Task) -> Unit
}

interface TodoItemState : RState {
    var isEditing: Boolean
    var editValue: String
}

class TodoItem(props: TodoItemProps) : RComponent<TodoItemProps, TodoItemState>(props) {
    override fun TodoItemState.init(props: TodoItemProps) {
        isEditing = false
        editValue = props.task.description
    }

    private fun getLiClass(): String {
        return listOf(
                if (props.task.isComplete) "completed" else null,
                if (state.isEditing) "editing" else null
        ).mapNotNull { it }.joinToString(" ")
    }

    private fun finishEditing() {
        setState { isEditing = false }
        props.onEdit(props.task, state.editValue)
    }

    private fun moveCursorToEndOnFocus(e: Event) {
        val temp = state.editValue
        val it = e.target as HTMLInputElement
        it.value = ""
        it.value = temp
    }

    override fun componentWillReceiveProps(nextProps: TodoItemProps) {
        // Make sure editValue state is updated with new description when we delete other tasks.
        // I feel like I wouldn't need this if I implemented the app better.
        setState { editValue = nextProps.task.description }
    }

    override fun RBuilder.render() {
        li(getLiClass()) {
            if (!state.isEditing) {
                div("view") {
                    input(
                            type = InputType.checkBox,
                            classes = "toggle"
                    ) {
                        attrs.checked = props.task.isComplete
                        attrs.onChangeFunction = { props.onCheck(props.task) }
                    }
                    label {
                        +props.task.description
                        attrs.onDoubleClickFunction = {
                            setState { isEditing = true }
                        }
                    }
                    button(classes = "destroy") {
                        attrs.onClickFunction = { props.onDelete(props.task) }
                    }
                }
            } else {
                input(
                        type = InputType.text,
                        classes = "edit"
                ) {
                    attrs.value = state.editValue
                    attrs.autoFocus = true
                    attrs.onFocusFunction = ::moveCursorToEndOnFocus
                    attrs.onChangeFunction = {
                        val target = it.target as HTMLInputElement
                        setState { editValue = target.value }
                    }
                    attrs.onKeyDownFunction = { e: Event ->
                        // https://discuss.kotlinlang.org/t/what-is-event-type-in-mouse-listeners/4938/2
                        val which = (e.asDynamic().nativeEvent as KeyboardEvent).which
                        when (which) {
                            ENTER_KEY_CODE -> finishEditing()
                            ESCAPE_KEY_CODE -> setState {
                                editValue = props.task.description // Reset task description if user presses ESC.
                                isEditing = false
                            }
                            else -> { /* do nothing */
                            }
                        }
                    }
                    attrs.onSubmitFunction = {
                        finishEditing()
                    }
                    attrs.onBlurFunction = {
                        finishEditing()
                    }
                }
            }
        }
    }
}

fun RBuilder.todoItem(task: Task, onCheck: (Task) -> Unit, onEdit: (Task, String) -> Unit, onDelete: (Task) -> Unit) = child(TodoItem::class) {
    attrs.task = task
    attrs.onCheck = onCheck
    attrs.onEdit = onEdit
    attrs.onDelete = onDelete
}