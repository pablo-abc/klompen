# Styling

You can add styles to the custom element by using the `set-styles!` function.

While you could perfectly add a `[:style]` tag to the template, the `set-styles!` function allows Klompen to add the styles as [constructable stylesheets](https://web.dev/constructable-stylesheets/) if they're supported by your browser (and defaults to style tags if not).

Example:

```clojure
(-> (create-ce)
    (set-styles!
    "button{border: none; background-color: pink;}")
    (set-html! [:button "Click me!"])
    (define! "my-button"))
```

In order not to write styles a strings, I recommend using [garden](https://github.com/noprompt/garden), which allows you to define styles with ClojureScript data structures.
