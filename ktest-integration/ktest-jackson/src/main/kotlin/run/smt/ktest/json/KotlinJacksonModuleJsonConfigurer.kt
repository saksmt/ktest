package run.smt.ktest.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class KotlinJacksonModuleJsonConfigurer : JsonConfigurer {
    override fun invoke(mapper: ObjectMapper): ObjectMapper {
        return mapper.registerModule(KotlinModule())
    }
}
