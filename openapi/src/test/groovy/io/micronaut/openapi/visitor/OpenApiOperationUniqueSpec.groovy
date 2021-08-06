package io.micronaut.openapi.visitor

import io.micronaut.openapi.AbstractOpenApiTypeElementSpec
import io.swagger.v3.oas.models.Operation

class OpenApiOperationUniqueSpec extends AbstractOpenApiTypeElementSpec {

    void "test OpenAPI does not append number if all operations are unique"() {
        given:
            buildBeanDefinition('test.MyBean', '''

package test;

import io.micronaut.http.annotation.*;
import java.util.List;

@Controller("/test1")
class TestController {
    @Get("/")
    String index() {
        return "Hello World";
    }
}

@Controller("/test2")
class TestController2 {
    @Get("/")
    String otherIndex() {
        return "Hello World";
    }
}

@jakarta.inject.Singleton
class MyBean {}
''')

            Operation firstOperation = AbstractOpenApiVisitor.testReference?.paths?.get("/test1")?.get
            Operation secondOperation = AbstractOpenApiVisitor.testReference?.paths?.get("/test2")?.get

        expect:
            firstOperation.getOperationId() == "indexGet"
            firstOperation.responses["200"].description == "indexGet 200 response"
            secondOperation.getOperationId() == "otherIndexGet"
            secondOperation.responses["200"].description == "otherIndexGet 200 response"
    }

    void "test OpenAPI generates unique operation ids"() {
        given:
            buildBeanDefinition('test.MyBean', '''

package test;

import io.micronaut.http.annotation.*;
import java.util.List;

@Controller("/test1")
class TestController {
    @Get("/")
    String index() {
        return "Hello World";
    }
}

@Controller("/test2")
class TestController2 {
    @Get("/")
    String index() {
        return "Hello World";
    }
}

@jakarta.inject.Singleton
class MyBean {}
''')

            Operation firstOperation = AbstractOpenApiVisitor.testReference?.paths?.get("/test1")?.get
            Operation secondOperation = AbstractOpenApiVisitor.testReference?.paths?.get("/test2")?.get

        expect:
            firstOperation.getOperationId() == "indexGet1"
            firstOperation.responses["200"].description == "indexGet1 200 response"
            secondOperation.getOperationId() == "indexGet2"
            secondOperation.responses["200"].description == "indexGet2 200 response"
    }

    void "test OpenAPI generates unique operation ids when there are some autogenerated conflicting with user"() {
        given:
            buildBeanDefinition('test.MyBean', '''

package test;

import io.swagger.v3.oas.annotations.*;
import io.micronaut.http.annotation.*;
import java.util.List;

@Controller("/test1")
class TestController {
    @Get("/")
    String index() {
        return "Hello World";
    }
}

@Controller("/test2")
class TestController2 {
    @Get("/")
    @Operation(operationId = "indexGet")
    String index() {
        return "Hello World";
    }
}

@jakarta.inject.Singleton
class MyBean {}
''')

            Operation operationWithGeneratedId = AbstractOpenApiVisitor.testReference?.paths?.get("/test1")?.get
            Operation operationWithUserId = AbstractOpenApiVisitor.testReference?.paths?.get("/test2")?.get

        expect:
            operationWithGeneratedId.getOperationId() == "indexGet1"
            operationWithGeneratedId.responses["200"].description == "indexGet1 200 response"
            operationWithUserId.getOperationId() == "indexGet"
            operationWithUserId.responses["200"].description == "indexGet 200 response"
    }

    void "test OpenAPI generates unique operation ids when there are some autogenerated conflicting with user 2"() {
        given:
            buildBeanDefinition('test.MyBean', '''

package test;

import io.swagger.v3.oas.annotations.*;
import io.micronaut.http.annotation.*;
import java.util.List;

@Controller("/test1")
class TestController {
    @Get("/")
    String index() {
        return "Hello World";
    }
}

@Controller("/test2")
class TestController2 {
    @Get("/")
    @Operation(operationId = "indexGet1")
    String index() {
        return "Hello World";
    }
}


@Controller("/test3")
class TestController3 {
    @Get("/")
    String index() {
        return "Hello World";
    }
}

@jakarta.inject.Singleton
class MyBean {}
''')

            Operation firstGenerated = AbstractOpenApiVisitor.testReference?.paths?.get("/test1")?.get
            Operation operationWithUserId = AbstractOpenApiVisitor.testReference?.paths?.get("/test2")?.get
            Operation secondGenerated = AbstractOpenApiVisitor.testReference?.paths?.get("/test3")?.get

        expect:
            firstGenerated.getOperationId() == "indexGet2"
            firstGenerated.responses["200"].description == "indexGet2 200 response"
            operationWithUserId.getOperationId() == "indexGet1"
            operationWithUserId.responses["200"].description == "indexGet1 200 response"
            secondGenerated.getOperationId() == "indexGet3"
            secondGenerated.responses["200"].description == "indexGet3 200 response"
    }

    void "test OpenAPI does not override user operation ids even when conflict"() {
        given:
            buildBeanDefinition('test.MyBean', '''

package test;

import io.swagger.v3.oas.annotations.*;
import io.micronaut.http.annotation.*;
import java.util.List;

@Controller("/test1")
class TestController {
    @Get("/")
    @Operation(operationId = "indexGet")
    String index() {
        return "Hello World";
    }
}

@Controller("/test2")
class TestController2 {
    @Get("/")
    @Operation(operationId = "indexGet")
    String otherIndex() {
        return "Hello World";
    }
}

@jakarta.inject.Singleton
class MyBean {}
''')

            Operation firstOperation = AbstractOpenApiVisitor.testReference?.paths?.get("/test1")?.get
            Operation secondOperation = AbstractOpenApiVisitor.testReference?.paths?.get("/test2")?.get

        expect:
            firstOperation.getOperationId() == "indexGet"
            firstOperation.responses["200"].description == "indexGet 200 response"
            secondOperation.getOperationId() == "indexGet"
            secondOperation.responses["200"].description == "indexGet 200 response"
    }

}