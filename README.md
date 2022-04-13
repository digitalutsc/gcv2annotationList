# gcv2annotationList

### Step 1: Download.

```
git clone https://github.com/AndrewEffendi/gcv2annotationList.git
```

### Step 2: Move into the gcv2annotationList directory.

```
cd gcv2annotationList
```

### Step 3: Compile the code.

```
javac -cp ./lib/*.jar gcv2anno.java
```

### Step 4: Run the code.

```
java -cp ./:./lib/json-simple-1.1.1.jar gcv2anno.java input.json output.json https://example.org/canvas/11
```

`input.json` is a output of [Google Cloud Vision OCR](https://cloud.google.com/vision/docs/). <br />
`output.json` is a output of gcv2anno. <br />
`https://example.org/canvas/11` is the link to the canvas we're annotating. <br />
