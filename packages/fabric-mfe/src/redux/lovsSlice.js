import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { fabricApi } from "../apis/fabric.api";

export const getLovs = createAsyncThunk(
  'lovs/getLovs',
  async () => {
    const res = await fabricApi.getLovData();
    return res;
  }
);

export const getSubDivisions = createAsyncThunk(
  'lovs/getSubDivisions',
  async (id) => {
    const res = await fabricApi.getSubDivisions(id);
    return res; 
  }
)

const initialState = {
  divisions: [],
  subDivisions: [],
  departments: [],
  classifications: [],
  loading: false,
  tagsLov: [],
}

export const lovsSlice = createSlice({
  name: 'lovs',
  initialState,
  reducers: {
    resetSubDivisions: (state) => {
      state.subDivisions = [];
    }
  },
  extraReducers: (builder) => {
    builder.addCase(getLovs.fulfilled, (state, action) => {
      state.classifications = action.payload[0].data.data;
      state.divisions = action.payload[1].data;
      state.departments = action.payload[2].data.data;
    });
    builder.addCase(getSubDivisions.pending, state => {
      state.loading = true;
    });
    builder.addCase(getSubDivisions.fulfilled, (state, action) => {
      state.subDivisions = action.payload.data.data;
      state.loading = false;
    });
    builder.addCase(getSubDivisions.rejected, state => {
      state.loading = false;
    });
  }
});

export const { resetSubDivisions } = lovsSlice.actions;

export default lovsSlice.reducer;