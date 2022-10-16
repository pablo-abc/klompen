# Templating

Klompen uses a hiccup-style syntax to define the markup of our custom element.

A really simple and static template could look like this:

```clojure
(def template [:div
               [:h1 "A title"]
               [:p "Content"]])
```

## Attributes, properties and event handlers

Attributes, properties and event handlers can be added to the element by using a map.

```clojure
(def template [:div {:id "an-id"} "Hello world!"])
```

Depending on the namespace of the key on the map, the behaviour will change.

Bare keywords without a namespace such as `:id` are assigned as attributes on the HTML element and can only be strings.

Keywords with a namespace of `bool` (e.g. `:bool/pressed`) are treated as boolean attributes. That is: a truthy value adds the attribute to the element, while a falsy value removes the attribute.

Keywords with a namespace of `on` (e.g. `:on/click`) are assigned as event listeners. Its value must be a function (Receives the host element as first argument, the event object as second argument). The name of the keyword itself refers to the event.

Keywords with a namespace of `prop` (e.g. `:prop/value`) are assigned as properties on the instance itself. They can be assigned any data type.

**TIP**: you can more easily assign ids and classes without using attributes by adding them to the tag name with a `#` for ids and `.` for classes. E.g. `[:div#an-id.a-class.another-class "Hello World"]`.

## Dynamic values

If you want the values of your attributes, properties or children to change depending on the state of your custom element, you can assign a function instead of a value to any of them. Whatever is returned from said function will be assigned as the value of the attribute/property/child.

This is specially useful when using reactive properties (assigned with `set-property!`). If any of these properties is read inside of the function, this function will re-run every time the property changes, updating the value of the attribute/property or the children of the element.

Example:

```clojure
(def template
  [[:input {:on/input
  ;; `value` is a reactive property on the custom element
  ;; First argument is the element instance
  ;; Second argument the event object
            #(set! (.-value %1) (.. %2 -target -value))}]
   [:span {:class
   ;; By assigning a function to the class attribute,
   ;; the class will be updated if the property `value`
   ;; changes on our custom element
           #(if (<= (.. % -value -length) 5)
              "red"
              "")}
    ;; The text content of the `span` element will be
    ;; the value of the `value` property on our custom
    ;; element and it'll be updated when the value
    ;; changes.
    #(.-value %)]])

(->
   (create-ce)
   ;; We add "value" as a reactive property
   (add-property! "value" "")
   ;; We set some styles
   (set-styles!
     ".red{color: red;} input{display: block;}")
   ;; We assign the template to the element
   (set-html! template)
   (define! "my-input"))

(render!
  js/document.body
  [:my-input])
```

**IMPORTANT:** Interpolation is not possible. For attributes and properties, you can assign either a value or a function. You can't interpolate both. In the case of children, the function **must** be the _only_ child of the element, since it replaces all the children of the element with the result of the function.
