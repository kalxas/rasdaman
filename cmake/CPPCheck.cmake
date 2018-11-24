# additional target to perform cppcheck run, requires cppcheck # get all project files
# HACK this workaround is required to avoid qml files checking ^_^

function(AddCPPCheckTarget THIRD_PARTY_DIR CPP_FILE_EXTENSIONS)
	find_package(CPPCheck)

	file(GLOB_RECURSE ALL_SOURCE_FILES ${CPP_FILE_EXTENSIONS})

	foreach (SOURCE_FILE ${ALL_SOURCE_FILES})
		string(FIND ${SOURCE_FILE} ${THIRD_PARTY_DIR} FOUND_IN_THIRD_PARTY_DIR)
		if (NOT ${FOUND_IN_THIRD_PARTY_DIR} EQUAL -1)
			list(REMOVE_ITEM ALL_SOURCE_FILES ${SOURCE_FILE})
		else()
			string(FIND ${SOURCE_FILE} "/cmake-" FOUND_IN_CMAKE_DIR)
			if (NOT ${FOUND_IN_CMAKE_DIR} EQUAL -1)
				list(REMOVE_ITEM ALL_SOURCE_FILES ${SOURCE_FILE})
			endif ()
		endif ()
	endforeach ()

	#TODO:Check parameters
	add_custom_target(cppcheck COMMAND ${CPPCHECK_EXECUTABLE} --enable=warning,performance,portability,information,missingInclude --std=c++11 --library=qt.cfg --template="[{severity}][{id}] {message} {callstack} \(On {file}:{line}\)" --verbose --quiet ${ALL_SOURCE_FILES})
endfunction(AddCPPCheckTarget)
