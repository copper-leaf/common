# Common

[ ![Download](https://api.bintray.com/packages/javaeden/Eden/Common/images/download.svg) ](https://bintray.com/javaeden/Eden/Common/_latestVersion)

A collection of utility classes and tools used throughout other Eden repositories, primarily targeted at type conversion
and managing options. This library targets Java 7 and works on Android, as well as plan Java projects.

## Options Management

---

### Overview

This library provides simple, yet powerful management of options for Java. In short, it allows you to extract data from 
a `Map<String, Object>` and coerce it to a field annotated with `@Option`. By default it supports the following types:

- the most commonly-used primitive types (and their Boxed counterparts)
    - `String`
    - `int`
    - `long`
    - `float`
    - `double`
    - `boolean`
- `org.json.JSONObject` and `org.json.JSONArray`<sup>*</sup>
- Java 8 `LocalDate`, `LocalTime`, and `LocalDateTime` classes
- Lists or Arrays of any of the above types.

This library was designed to be easy to extend, and useful for a variety of platforms. It is at the heart of 
[Orchid](https://orchid.netlify.com), but is small and fast enough to be used on Android. 

<sup>*</sup> _The `org.json` classes used by Android is not compatible with this library, as the classes loaded by the 
Android system are a much older version that is not API-compatible with any recent version of `org.json`._ 

### Basic Usage

Take any class, and annotate any fields with `@Option`:

```java
public class Options {

  @Option("anotherName") public int intVal;
  @Option public boolean boolVal;
  @Option public float floatVal;
  
}
```

And pass an instance of that Class to an `Extractor` with the data to be extracted:

```java
Options optionsHolder = new Options();
Map<String, Object> data = getData();
DefaultExtractor.getInstance().extractOptions(optionsHolder, data);
```

Your options-holder classes can have parent classes, and all parent-fields will be extracted as well. Every 
`@Option`-annotation is assigned during the extraction process, so no stale data will be left if the same object is 
extracted multiple times with different data.

### Options Names

By default, the extractor will use the field name to lookup the property in the provided options data. These fields 
should either be public, or have a bean-style setter to set the resulting value. 

In some situations, you may wish to set the option from an option value that is different from the field name. In this 
case you can pass a different name to the `@Option` annotation, and it will use that value instead. This is also useful
when using Proguard to obfuscate the compiled bytecode to keep the original option name after the field name has been 
mangled. 

You are also free to use the same option key for multiple fields, and each one will be coerced into that field's type.

### Type Conversion

The extract will do whatever it can to convert the data in the provided options into the field type defined. Here's a 
few examples<sup>*</sup>:

- Parse a String number to an `int`
- Parse a "truthy" value to a `boolean`. true values can be String the literal `true`, non-zero numbers, and non-null 
    objects
- Parse a date or time string into `LocalDate`, `LocalTime`, or `LocalDateTime`. The exact format is flexible, and 
    doesn't necessarily need to fit exactly within the defined format
- Convert a JSON Object into a Map, and vice-versa
- Get a List consisting of any other types

<sup>*</sup> _Check out the unit tests to see all possible conversions provided by default._

### Default Values

All fields are assigned during each extraction, and in cases where the value is not provided, a default will be assigned
for you. For primitive types, this default value can be set using the following annotations:

- `@StringDefault("default value")`
- `@IntDefault(1)`
- `@LongDefault(1L)`
- `@DoubleDefault(1.1)`
- `@FloatDefault(1.1f)`
- `@BooleanDefault(true)`

These same annotations will also be used for lists or arrays of these same types, and you can provide an array of values
to the annotation as well for these cases.

Other extracted types can define their own rules for default values. For example, the `LocalDate` class defaults to 
`LocalDate.now()`. 

### String Helpers

Most values are converted to String before converting back to the field's type. All objects converted to a String are
passed to a StringHelper which can process that value before returning it back to the option field. This could be useful
to inject values dynamically into the string value<sup>*</sup>, or for i18n purposes.

<sup>*</sup> _This comes already set up with a helper for Clog formatting._

### Archetypes

While this is nice, this library really gets awesome with Archetypes. Archetypes allow you to pull options from other 
sources dynamically, rather than just those passed to the Extractor. These "archetypal options" are then used as 
fallbacks in the case that a value is not provides in the options passed to the extractor. For example:

```java
@Archetype(value = ResourcesArchetype.class, key = "options")
public class Options {
  ...
}
```

Now, the extractor will create an instance of `ResourcesArchetype`<sup>*</sup> and pass it the instance of `Options` and 
the `key`, allowing it to do whatever it needs to based on its current context to load additional options. This makes it 
possible to decouple the loading of options data from the classes consuming them.

You can use multiple Archetypes on a class, either by adding them to the `@Archetypes` container annotation or by using
Java 8 repeated annotations. Archetypes that are defined first on that class will take precedence over those defined
later.

You can also define Archetypes on your options-holder's parent classes. Options from Archetypes on child classes will 
take precedence over the options from Archetypes on parent classes, and repeated annotations are evaluated on a 
per-class basis.

<sup>*</sup> _Note that Archetypes are highly dependant on your application, and as such none are provided natively._

### Annotation Processor

The current implementation works via reflection and is pretty fast, but an Annotation Processor is in the works to make
it even faster. 