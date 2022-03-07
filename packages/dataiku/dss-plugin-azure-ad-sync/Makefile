# Public variable to be set by the user in the Makefile
TARGET_DSS_VERSION=8.0

# evaluate additional variable
plugin_id=`cat plugin.json | python -c "import sys, json; print(str(json.load(sys.stdin)['id']).replace('/',''))"`
plugin_version=`cat plugin.json | python -c "import sys, json; print(str(json.load(sys.stdin)['version']).replace('/',''))"`
archive_file_name="dss-plugin-${plugin_id}-${plugin_version}.zip"
remote_url=`git config --get remote.origin.url`
last_commit_id=`git rev-parse HEAD`


plugin:
	@echo "[START] Archiving plugin to dist/ folder..."
	@cat plugin.json | json_pp > /dev/null
	@rm -rf dist
	@mkdir dist
	@echo "{\"remote_url\":\"${remote_url}\",\"last_commit_id\":\"${last_commit_id}\"}" > release_info.json
	@git archive -v -9 --format zip -o dist/${archive_file_name} HEAD
	@zip -u dist/${archive_file_name} release_info.json
	@rm release_info.json
	@echo "[SUCCESS] Archiving plugin to dist/ folder: Done!"

unit-tests:
	@echo "[START] Running unit tests..."
	@( \
		python3 -m venv env/; \
		source env/bin/activate; \
		pip3 install --upgrade pip; \
		pip install --no-cache-dir -r tests/python/requirements.txt; \
		pip install --no-cache-dir -r code-env/python/spec/requirements.txt; \
		export PYTHONPATH="$(PYTHONPATH):$(PWD)/python-lib"; \
                pytest -o junit_family=xunit2 --junitxml=unit.xml tests/python/unit || true; \
		deactivate; \
	)
	@echo "[SUCCESS] Running unit tests: Done!"

integration-tests:
	@echo "[START] Running integration tests..."
	# TODO add integration tests
	@echo "[SUCCESS] Running integration tests: Done!"

tests: unit-tests integration-tests

dist-clean:
	rm -rf dist
