package todolist

import kotlinx.html.InputType
import kotlinx.html.js.*
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.*

private val PLACEHOLDER = "What needs to be done?"

interface TaskBarProps : RProps {
    var onSubmit: (String) -> Unit
}

interface TaskBarState : RState {
    var taskBarValue: String
}

class TaskBar(props: TaskBarProps) : RComponent<TaskBarProps, TaskBarState>(props) {
    override fun TaskBarState.init(props: TaskBarProps) {
        taskBarValue = ""
    }

    override fun RBuilder.render() {
        form {
            attrs.onSubmitFunction = { e ->
                e.preventDefault()
                if (!state.taskBarValue.isEmpty()) {
                    props.onSubmit(state.taskBarValue)
                    setState { taskBarValue = "" }
                }
            }
            input(
                    type = InputType.text,
                    classes = "new-todo"
            ) {
                attrs.value = state.taskBarValue
                attrs.placeholder = PLACEHOLDER
                attrs.onChangeFunction = {
                    val target = it.target as HTMLInputElement
                    setState { taskBarValue = target.value }
                }
            }
        }
    }
}

fun RBuilder.todoNewBar(onSubmit: (String) -> Unit) = child(TaskBar::class) {
    attrs.onSubmit = onSubmit
}