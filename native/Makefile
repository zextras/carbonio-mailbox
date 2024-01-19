# SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only

SRC     = src/main

BUILD   = target

BUILD_ROOT := $(shell pwd)

ifneq (${JAVA_HOME},)
JAVA_HOME_ = ${JAVA_HOME}
else
JAVA_HOME_ = $(subst /bin/javac,,$(realpath $(shell which javac)))
endif

SHARED := -shared

PLATFORM = $(uname -s)

ifeq ($(PLATFORM), 'Darwin')
	JAVAINC := -I$(JAVA_HOME_)/include -I$(JAVA_HOME_)/include/darwin
else
	JAVAINC := -I$(JAVA_HOME_)/include -I$(JAVA_HOME_)/include/linux
endif

SHARED_EXT := so
PUSHED_EXT := so.Linux.i386
CF := -fPIC -g
CC := gcc

all: FORCE
	$(MAKE) $(BUILD)/libnative.$(SHARED_EXT)

FORCE: ;

$(BUILD)/libnative.$(SHARED_EXT): $(BUILD)/IO.o $(BUILD)/zjniutil.o
	$(CC) $(CF) $(LIB_OPTS) $(SHARED) -o $@ $^

$(BUILD)/%.o: $(SRC)/native/%.c
	$(CC) $(CF) $(MACDEF) $(JAVAINC) -I$(BUILD) -Wall -Wmissing-prototypes -c -o $@ $<

$(BUILD)/zjniutil.o: $(SRC)/native/zjniutil.c $(SRC)/native/zjniutil.h

$(BUILD)/IO.o: $(SRC)/native/IO.c $(BUILD)/com_zimbra_znative_IO.h $(SRC)/native/zjniutil.h

clean:
	$(RM) -r $(BUILD)

.PHONY: all push clean
