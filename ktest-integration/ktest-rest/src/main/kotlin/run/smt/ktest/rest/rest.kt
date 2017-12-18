package run.smt.ktest.rest

import run.smt.ktest.config.config
import run.smt.ktest.rest.impl.RestContextRegistry

/**
 * Entry point for REST API DSL
 */
val rest = RestContextRegistry(config)
