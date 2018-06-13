package run.smt.ktest.json

import com.fasterxml.jackson.databind.ObjectMapper

interface JsonConfigurer : (ObjectMapper) -> ObjectMapper {
    override fun invoke(mapper: ObjectMapper): ObjectMapper
}
