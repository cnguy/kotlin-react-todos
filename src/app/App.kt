package app

import react.*
import react.dom.*
import logo.*
import todolist.todoList

class App : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        div("App-header") { logo() }
        todoList()
        footer("info") {
            div { +"Double-click to edit a todo" }
            div {
                +"Created by "
                a("https://github.com/cnguy") {
                    +"Chau Nguyen"
                }
            }
            div {
                a("https://github.com/cnguy/kotlin-react-todomvc") {
                    +"GitHub Repository"
                }
            }
        }
    }
}

fun RBuilder.app() = child(App::class) {}
