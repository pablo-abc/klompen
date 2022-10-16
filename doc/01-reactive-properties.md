# Reactive Properties

Reactive properties can be assigned to an element by using the `add-property!` function. Its first argument is the property name and the second argument is its default value (required). Optionally you can add an options map with the following keys:

- `:type`: a function that will transform the value to the expected type. By default it is `js/String`, but if, for example, your value needs to be a number, then you can override it with `js/Number`.
- `:attribute`: the attribute name (string) that maps to the property. By default it's the lower-case property name. E.g. a property `someValue` would map to an attribute `somevalue`. If you don't want the property to map to an attribute, you can assign `false` to it.

Example:

```clojure
(-> (create-ce)
    (add-property! "count" 0 {:type js/Number})
    (add-property! "name" "")
    (define! "my-element"))
```
